package uk.gov.justice.digital.hmpps.csr.api.dto

import java.util.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "ShiftNotification")
data class ShiftNotificationDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "User unique identifier", position = 1, example = "AB102CD")
        @JsonProperty("quantumId")
        val quantumId: String,

        @ApiModelProperty(required = true, value = "When the shift notification was created", position = 2, example = "2020-04-20T17:45:55")
        @JsonProperty("createdAt")
        val createdAt: LocalDateTime,

        @ApiModelProperty(required = true, value = "Staff ID number", position = 3, example = "123")
        @JsonProperty("staffId")
        val staffId: Int,

        @ApiModelProperty(required = true, value = "Date of shift", position = 4, example = "2020-08-22")
        @JsonProperty("shiftDate")
        val shiftDate: LocalDate,

        @ApiModelProperty(required = true, value = "Date shift was last modified", position = 5, example = "2020-08-22T09:45:55")
        @JsonProperty("lastModified")
        val lastModified: LocalDateTime,

        @ApiModelProperty(required = true, value = "Type of shift", position = 6, example = "SHIFT")
        @JsonProperty("shiftType")
        val shiftType: String,

        @ApiModelProperty(required = true, value = "Action type of notification", position = 3, example = "EDIT")
        @JsonProperty("actionType")
        val actionType: String
) {
        companion object {

                fun from(shiftNotifications: Collection<ShiftNotification>): Collection<ShiftNotificationDto> {
                        return shiftNotifications.map {
                                from(it)
                        }
                }

                fun from(it: ShiftNotification): ShiftNotificationDto {
                        return ShiftNotificationDto(
                                it.quantumId,
                                it.createdAt,
                                it.staffId,
                                it.shiftDate,
                                it.lastModified,
                                ShiftType.fromInt(it.shiftType)!!.shiftType,
                                ActionType.fromInt(it.actionType)!!.action
                        )
                }

        }
}
