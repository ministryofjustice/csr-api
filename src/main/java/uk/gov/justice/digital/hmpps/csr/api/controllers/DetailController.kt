package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.service.DetailService
import java.time.LocalDate

@Api(tags = ["details"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class DetailController(private val detailService: DetailService) {

    @ApiOperation(value = "Retrieve all details for a user between two dates")
    @GetMapping("/user/details")
    fun getDetailsByUser(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<Collection<DetailDto>> {
        return ResponseEntity.ok(detailService.getStaffDetails(from, to))
    }

    @ApiOperation(value = "Retrieve all modified details for a plan unit")
    @GetMapping("planUnit/{planUnit}/details/modified")
    fun getModifiedDetailsByPlanUnit(@PathVariable planUnit: String): ResponseEntity<Collection<DetailDto>> {
        return ResponseEntity.ok(detailService.getModifiedDetailsByPlanUnit(planUnit))
    }

}

