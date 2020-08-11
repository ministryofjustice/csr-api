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

        @ApiModelProperty(required = true, value = "Date of shift", position = 2, example = "2020-08-22")
        @JsonProperty("shiftDate")
        val shiftDate: LocalDate,

        @ApiModelProperty(required = true, value = "Date shift was last modified", position = 3, example = "2020-08-22T09:45:55")
        @JsonProperty("lastModified")
        val shiftModified: LocalDateTime,

        @ApiModelProperty(required = true, value = "Type of shift", position = 4, example = "SHIFT")
        @JsonProperty("shiftType")
        val shiftType: String,

        @ApiModelProperty(required = true, value = "Action type of notification", position = 5, example = "EDIT")
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
                                it.shiftDate,
                                it.shiftModified,
                                ShiftType.fromInt(it.shiftType)!!.shiftType,
                                ActionType.fromInt(it.actionType)!!.action
                        )
                }

        }
}
