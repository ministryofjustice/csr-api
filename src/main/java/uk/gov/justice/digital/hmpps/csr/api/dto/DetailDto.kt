package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.LocalDate
import java.time.LocalDateTime

@ApiModel(description = "DetailDto")
data class DetailDto @JsonCreator constructor(
        @ApiModelProperty(value = "User unique identifier", example = "AB102CD")
        @JsonProperty("quantumId")
        val quantumId: String?,

        @ApiModelProperty(value = "Date shift was last modified", example = "2020-08-22T09:45:55")
        @JsonProperty("shiftModified")
        val shiftModified: LocalDateTime?,

        @ApiModelProperty(value = "Date of shift the detail relates to", example = "2020-08-22")
        @JsonProperty("shiftDate")
        val shiftDate: LocalDate?,

        @ApiModelProperty(value = "Type of shift the detail relates to", example = "OVERTIME")
        @JsonProperty("shiftType")
        val shiftType: ShiftType?,

        @ApiModelProperty(value = "Detail start time in seconds from midnight", example = "6400")
        @JsonProperty("detailStart")
        val detailStart: Long?,

        @ApiModelProperty(value = "Detail end time in seconds from midnight", example = "8310")
        @JsonProperty("detailEnd")
        val detailEnd: Long?,

        @ApiModelProperty(value = "Detail activity", example = "Canteen watch")
        @JsonProperty("activity")
        val activity: String?,

        @ApiModelProperty(value = "Type of notification action", example = "EDIT")
        @JsonProperty("actionType")
        val actionType: ActionType?
) {
        companion object {

                fun from(it: Detail): DetailDto {
                        return DetailDto(
                                it.quantumId,
                                it.shiftModified,
                                it.shiftDate,
                                it.shiftType?.let { type -> ShiftType.from(type) },
                                it.startTimeInSeconds,
                                it.endTimeInSeconds,
                                it.activity,
                                it.actionType?.let { type -> ActionType.from(type) }
                        )
                }

                fun from(details: Collection<Detail>): Collection<DetailDto> {
                        return details.map { from(it) }
                }


        }
}
