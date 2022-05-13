package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
import uk.gov.justice.digital.hmpps.csr.api.service.DetailService
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext
import java.time.LocalDate

@Tag(name = "details", description = "Details of shifts from CSR")
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class DetailController(private val detailService: DetailService) {

  @Operation(summary = "Retrieve all details for a user between two dates")
  @Parameter(
    `in` = ParameterIn.HEADER,
    name = "X-Region",
    description = "the number of the required region, 1-6",
    required = true
  )
  @GetMapping("/user/details")
  @Deprecated("Deprecated in favour of the endpoint which specifies region in the url")
  fun getDetailsByUser(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
  ): ResponseEntity<Collection<DetailDto>> {
    val details = detailService.getStaffDetails(from, to)
    return ResponseEntity.ok(details)
  }

  @Operation(summary = "Retrieve all details for a user between two dates")
  @GetMapping("/user/details/{region}")
  fun getDetailsByUserWithRegion(
    @PathVariable @Schema(
      description = "the number of the required region",
      allowableValues = ["1", "2", "3", "4", "5", "6"]
    ) region: Int,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
  ): ResponseEntity<Collection<DetailDto>> {
    RegionContext.setRegion(region.toString())

    val details = detailService.getStaffDetails(region, from, to)
    return ResponseEntity.ok(details)
  }

  @Operation(summary = "Retrieve all modified shifts for a plan unit", deprecated = true)
  @Parameter(
    `in` = ParameterIn.HEADER,
    name = "X-Region",
    description = "the number of the required region, 1-6",
    required = true
  )
  @GetMapping("/planUnit/{planUnit}/shifts/updated")
  @Deprecated("/updates now returns all notification data for the region")
  fun getModifiedShiftByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedShiftsByPlanUnit(planUnit))
  }

  @Operation(summary = "Retrieve all modified details for a plan unit", deprecated = true)
  @Parameter(
    `in` = ParameterIn.HEADER,
    name = "X-Region",
    description = "the number of the required region, 1-6",
    required = true
  )
  @GetMapping("/planUnit/{planUnit}/details/updated")
  @Deprecated("/updates now returns all notification data for the region")
  fun getModifiedDetailByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedDetailsByPlanUnit(planUnit))
  }

  @Operation(summary = "Retrieve all modified records")
  @GetMapping("/updates/{region}")
  fun getModified(
    @PathVariable
    region: Int
  ): List<DetailDto> {
    RegionContext.setRegion(region.toString())

    return detailService.getModified()
  }

  @Operation(
    summary = "Delete specific modified records",
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = Long::class))
        )
      ]
    ),
  )
  @PutMapping("/updates/{region}")
  fun deleteProcessed(
    @PathVariable @Schema(description = "the number of the required region, 1-6") region: Int,
    @RequestBody @Schema(description = "List of ids to delete") ids: List<Long>
  ): String {
    RegionContext.setRegion(region.toString())

    return detailService.deleteProcessed(ids)
  }

  @Operation(
    summary = "Delete all notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role"
  )
  @PutMapping("/updates/delete-all/{region}")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun deleteAll(@PathVariable @Schema(description = "the number of the required region, 1-6") region: Int): String {
    RegionContext.setRegion(region.toString())

    return detailService.deleteAll()
  }

  @Operation(
    summary = "Delete old notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role"
  )
  @PutMapping("/updates/delete-old/{region}")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun setProcessed(
    @PathVariable @Schema(description = "the number of the required region, 1-6") region: Int,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "Delete all rows added before this date", example = "2022-03-28")
    date: LocalDate
  ): String {
    RegionContext.setRegion(region.toString())

    return detailService.deleteOld(date)
  }
}
