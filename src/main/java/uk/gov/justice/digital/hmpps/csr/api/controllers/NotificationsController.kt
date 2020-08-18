package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.csr.api.service.NotificationService


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

}

