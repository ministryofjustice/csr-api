package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.service.DetailService
import java.time.LocalDate

@Tag(name = "details", description = "Get details of shifts from CSR")
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

  @Operation(summary = "Retrieve all modified shifts for a plan unit")
  @GetMapping("planUnit/{planUnit}/shifts/updated")
  fun getModifiedShiftByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedShiftsByPlanUnit(planUnit))
  }

  @Operation(summary = "Retrieve all modified details for a plan unit")
  @GetMapping("planUnit/{planUnit}/details/updated")
  fun getModifiedDetailByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
    return ResponseEntity.ok(detailService.getModifiedDetailsByPlanUnit(planUnit))
  }
}
