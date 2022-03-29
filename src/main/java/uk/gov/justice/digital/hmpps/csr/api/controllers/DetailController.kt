package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import java.time.LocalDate

@Tag(
  name = "details",
  description = "Details of shifts from CSR. NOTE: All of these endpoints use the header value 'X-Region' to select the required region"
)
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class DetailController(private val detailService: DetailService) {

  @Operation(summary = "Retrieve all details for a user between two dates")
  @GetMapping("/user/details")
  fun getDetailsByUser(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
  ): ResponseEntity<Collection<DetailDto>> {
    val details = detailService.getStaffDetails(from, to)
    return ResponseEntity.ok(details)
  }

  @Operation(summary = "Retrieve all modified shifts for a plan unit", deprecated = true)
  @GetMapping("/planUnit/{planUnit}/shifts/updated")
  fun getModifiedShiftByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedShiftsByPlanUnit(planUnit))
  }

  @Operation(summary = "Retrieve all modified details for a plan unit", deprecated = true)
  @GetMapping("/planUnit/{planUnit}/details/updated")
  fun getModifiedDetailByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedDetailsByPlanUnit(planUnit))
  }

  @Operation(summary = "Retrieve all modified records")
  @GetMapping("/updates")
  fun getModified(): List<DetailDto> = detailService.getModified()

  @Operation(summary = "Delete specific modified records")
  @PutMapping("/updates")
  fun deleteProcessed(@RequestBody @Schema(description = "List of ids to delete") ids: List<Long>) {
    detailService.deleteProcessed(ids)
  }

  @Operation(
    summary = "Delete all notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role"
  )
  @PutMapping("/updates/delete-all")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun deleteAll() {
    detailService.deleteAll()
  }

  @Operation(
    summary = "Delete old notification records",
    description = "Intended for manual use only, requires CMD_ADMIN role"
  )
  @PutMapping("/updates/delete-old")
  @PreAuthorize("hasRole('ROLE_CMD_ADMIN')")
  fun setProcessed(
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Delete all rows added before this date")
    @Parameter(description = "Delete all rows added before this date", example = "2022-03-28")
    date: LocalDate
  ) {
    detailService.deleteOld(date)
  }
}
