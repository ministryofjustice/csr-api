package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.service.DetailService
import uk.gov.justice.digital.hmpps.csr.api.utils.region.CsrConfiguration
import java.time.LocalDate

@Tag(name = "details", description = "Details of shifts from CSR")
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ROLE_CMD')")
class DetailController(
  private val detailService: DetailService,
  private val sqlRepository: SqlRepository,
  csrConfiguration: CsrConfiguration,
) {
  private val regionMap = csrConfiguration.regions.associate { it.name to it.schema }

  @Operation(summary = "Retrieve all details for a user between two dates")
  @GetMapping("/user/details/{region}")
  fun getDetailsByUserWithRegion(
    @PathVariable
    @Schema(
      description = "the number of the required region",
      allowableValues = ["1", "2", "3", "4", "5", "6"],
    )
    region: Int,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    from: LocalDate,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    to: LocalDate,
  ): ResponseEntity<Collection<DetailDto>> {
    setDatabaseSchema(region)

    val details = detailService.getStaffDetails(from, to)
    return ResponseEntity.ok(details)
  }

  @Operation(summary = "Retrieve all modified records")
  @GetMapping("/updates/{region}")
  fun getModified(
    @PathVariable
    region: Int,
  ): List<DetailDto> {
    setDatabaseSchema(region)

    return detailService.getModified()
  }

  @Operation(
    summary = "Delete specific modified records",
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = Long::class)),
        ),
      ],
    ),
  )
  @PutMapping("/updates/{region}")
  fun deleteProcessed(
    @PathVariable
    @Schema(description = "the number of the required region, 1-6")
    region: Int,
    @RequestBody
    @Schema(description = "List of ids to delete")
    ids: List<Long>,
  ) {
    setDatabaseSchema(region)

    detailService.deleteProcessed(ids)
  }

  @Operation(
    summary = "Delete all notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role",
  )
  @PutMapping("/updates/delete-all/{region}")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun deleteAll(
    @PathVariable
    @Schema(description = "the number of the required region, 1-6")
    region: Int,
  ): String {
    setDatabaseSchema(region)

    return detailService.deleteAll()
  }

  @Operation(
    summary = "Delete old notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role",
  )
  @PutMapping("/updates/delete-old/{region}")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun setProcessed(
    @PathVariable
    @Schema(description = "the number of the required region, 1-6")
    region: Int,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "Delete all rows added before this date", example = "2022-03-28")
    date: LocalDate,
  ): String {
    setDatabaseSchema(region)

    return detailService.deleteOld(date)
  }

  private fun setDatabaseSchema(region: Int) {
    sqlRepository.setSchema(regionMap[region]!!)
  }
}
