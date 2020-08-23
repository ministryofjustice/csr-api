package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftOvertime
import java.time.LocalDate


@ApiModel(description = "ShiftOvertimeDto")
data class ShiftOvertimeDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Date of detail", position = 1, example = "01/01/2010")
        @JsonProperty("date")
        val date: LocalDate,

        @ApiModelProperty(required = true, value = "Start time in seconds from midnight", position = 2, example = "6400")
        @JsonProperty("start")
        val start: Long,

        @ApiModelProperty(required = true, value = "End time in seconds from midnight", position = 3, example = "83100")
        @JsonProperty("end")
        val end: Long,

        @ApiModelProperty(required = true, value = "Type of shift", position = 4, example = "overtime")
        @JsonProperty("type")
        val type: String,

        @ApiModelProperty(required = true, value = "Detail of shift task", position = 5, example = "Canteen watch")
        @JsonProperty("task")
        val task: String
) {
        companion object {

                fun from(it: ShiftOvertime): ShiftOvertimeDto {
                        return ShiftOvertimeDto(
                                it.date,
                                it.detailStartTimeInSeconds,
                                it.detailEndTimeInSeconds,
                                ShiftType.OVERTIME.name,
                                it.task
                        )
                }

                fun from(overtimes: Collection<ShiftOvertime>): Collection<ShiftOvertimeDto> {
                        return overtimes.map { from(it) }
                }

        }
}
