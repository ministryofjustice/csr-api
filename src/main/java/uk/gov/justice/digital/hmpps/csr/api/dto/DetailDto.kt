package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.DetailType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.LocalDateTime

@ApiModel(description = "DetailDto")
data class DetailDto @JsonCreator constructor(
        @ApiModelProperty(value = "User unique identifier", example = "AB102CD")
        @JsonProperty("quantumId")
        val quantumId: String?,

        @ApiModelProperty(value = "Date shift was last modified", example = "2020-08-22T09:45:55")
        @JsonProperty("shiftModified")
        val shiftModified: LocalDateTime?,

        @ApiModelProperty(value = "Type of shift the detail relates to", example = "OVERTIME")
        @JsonProperty("shiftType")
        val shiftType: ShiftType,

        @ApiModelProperty(value = "Detail start date time", example = "2020-08-22T09:15:00")
        @JsonProperty("detailStart")
        val detailStart: LocalDateTime,

        @ApiModelProperty(value = "Detail end date time", example = "2020-08-22T09:15:00")
        @JsonProperty("detailEnd")
        val detailEnd: LocalDateTime,

        @ApiModelProperty(value = "Detail activity", example = "Canteen watch")
        @JsonProperty("activity")
        val activity: String?,

        @ApiModelProperty(value = "Type of detail", example = "Unspecific")
        @JsonProperty("detailType")
        val detailType: DetailType?,

        @ApiModelProperty(value = "Type of modification action", example = "EDIT")
        @JsonProperty("actionType")
        val actionType: ActionType?

) {
        companion object {

                fun from(detail: Detail, detailStart: LocalDateTime, detailEnd: LocalDateTime): DetailDto {
                        return DetailDto(
                                detail.quantumId,
                                detail.shiftModified,
                                ShiftType.from(detail.shiftType),
                                detailStart,
                                detailEnd,
                                detail.activity,
                                detail.detailType?.let { type -> DetailType.from(type) },
                                detail.actionType?.let { type -> ActionType.from(type) }
                        )
                }
        }
}
