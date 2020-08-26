package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.csr.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.csr.api.service.NotificationService

@Api(tags = ["notifications"])
@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class NotificationController(private val notificationService: NotificationService) {

    @ApiOperation(value = "Retrieve all notifications for a plan unit")
    @GetMapping("notifications/{planUnit}")
    fun getNotificationsByShift(@PathVariable planUnit: String): ResponseEntity<Collection<NotificationDto>> {
        return ResponseEntity.ok(notificationService.getShiftAndDetailNotifications(planUnit))
    }

}

