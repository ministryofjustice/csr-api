package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.DetailNotification
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.csr.api.service.NotificationService
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

        @ApiModelProperty(required = true, value = "Type of shift", position = 7, example = "overtime")
        @JsonProperty("type")
        val shiftType: String,

        @ApiModelProperty(required = true, value = "Type of notification action", position = 7, example = "edit")
        @JsonProperty("actionType")
        val actionType: String = ActionType.EDIT.action
) {
        companion object {

                fun fromShift(shiftNotifications: Collection<ShiftNotification>): Collection<NotificationDto> {
                        return shiftNotifications.map {
                                fromShift(it)
                        }
                }
                fun fromDetail(detailNotification: Collection<DetailNotification>): Collection<NotificationDto> {
                        return detailNotification.map {
                                fromDetail(it)
                        }
                }

                fun fromShift(it: ShiftNotification): NotificationDto {
                        return NotificationDto(
                                it.quantumId,
                                it.shiftDate,
                                it.lastModified,
                                null,
                                null,
                                null,

                                ShiftType.fromInt(it.shiftType)
                                        ?.shiftType
                                        ?: run {
                                                log.warn("No shift type. Overwriting as shift")
                                                ShiftType.SHIFT.shiftType
                                        },
                                ActionType.fromInt(it.actionType)
                                        ?.action
                                        ?: run {
                                                log.warn("No Action Type. Overwrite with edit")
                                                ActionType.EDIT.action
                                        }
                        )
                }

                fun fromDetail(it: DetailNotification): NotificationDto {
                        return NotificationDto(
                                it.quantumId,
                                it.shiftDate,
                                it.lastModified,
                                it.detailStartTimeInSeconds,
                                it.detailEndTimeInSeconds,
                                it.task,


                                ShiftType.fromInt(it.shiftType)
                                        ?.shiftType
                                        ?: run {
                                                log.warn("No shift type. Overwriting as shift")
                                                ShiftType.SHIFT.shiftType
                                        },
                                ActionType.EDIT.action
                        )
                }

                private val log = LoggerFactory.getLogger(NotificationService::class.java)

        }
}
