package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftOvertimesDto
import uk.gov.justice.digital.hmpps.csr.api.service.NotificationService
import java.time.LocalDate


@Api(tags = ["notifications"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class NotificationController(val notificationService: NotificationService) {

    @ApiOperation(value = "Retrieve all notifications for a user between two dates")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "OK", response = ShiftNotificationDto::class)
    ])

    @GetMapping("notifications/{planUnit}")
    fun getNotificationsByShift(@PathVariable planUnit: String): ResponseEntity<Collection<ShiftNotificationDto>> {
        return ResponseEntity.ok(notificationService.getShiftNotificationsAndDetails(planUnit))
    }

    @GetMapping("shifts/overtime/detail")
    fun getNotificationsByShift(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<ShiftOvertimesDto> {
        return ResponseEntity.ok(notificationService.getOvertime(from, to))
    }

}

