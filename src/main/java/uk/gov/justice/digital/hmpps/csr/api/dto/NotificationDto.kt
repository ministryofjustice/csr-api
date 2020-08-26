package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.DetailNotification
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "ShiftNotification")
data class NotificationDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "User unique identifier", position = 1, example = "AB102CD")
        @JsonProperty("quantumId")
        val quantumId: String,

        @ApiModelProperty(required = true, value = "Date of shift", position = 2, example = "2020-08-22")
        @JsonProperty("shiftDate")
        val shiftDate: LocalDate,

        @ApiModelProperty(required = true, value = "Date shift was last modified", position = 3, example = "2020-08-22T09:45:55")
        @JsonProperty("lastModified")
        val shiftModified: LocalDateTime,

        @ApiModelProperty(required = true, value = "Start time in seconds from midnight", position = 4, example = "36000")
        @JsonProperty("detailStartTimeInSeconds")
        val taskStart: Long?,

        @ApiModelProperty(required = true, value = "End time in seconds from midnight", position = 5, example = "83000")
        @JsonProperty("detailEndTimeInSeconds")
        val taskEnd: Long?,

        @ApiModelProperty(required = true, value = "Detail of shift task", position = 6, example = "Gym watch")
        @JsonProperty("task")
        val task: String?,

        @ApiModelProperty(required = true, value = "Type of shift", position = 7, example = "OVERTIME")
        @JsonProperty("type")
        val shiftType: ShiftType,

        @ApiModelProperty(required = true, value = "Type of notification action", position = 8, example = "EDIT")
        @JsonProperty("actionType")
        val actionType: ActionType
) {
        companion object {

                fun fromShift(shiftNotifications: Collection<ShiftNotification>): Collection<NotificationDto> {
                        return shiftNotifications.map {
                                from(it)
                        }
                }

                fun fromDetail(detailNotification: Collection<DetailNotification>): Collection<NotificationDto> {
                        return detailNotification.map {
                                from(it)
                        }
                }

                private fun from(it: ShiftNotification): NotificationDto {
                        return NotificationDto(
                                it.quantumId,
                                it.shiftDate,
                                it.lastModified,
                                null,
                                null,
                                null,
                                ShiftType.from(it.shiftType),
                                ActionType.from(it.actionType)
                        )
                }

                private fun from(it: DetailNotification): NotificationDto {
                        return NotificationDto(
                                it.quantumId,
                                it.shiftDate,
                                it.lastModified,
                                it.detailStartTimeInSeconds,
                                it.detailEndTimeInSeconds,
                                it.task,
                                ShiftType.from(it.shiftType),
                                ActionType.EDIT
                        )
                }
        }
}
