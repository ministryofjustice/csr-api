package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.LocalDate

@ApiModel(description = "DetailDto")
data class DetailDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Date of detail", position = 1, example = "01/01/2010")
        @JsonProperty("date")
        val date: LocalDate,

        @ApiModelProperty(required = true, value = "Start time in seconds from midnight", position = 2, example = "6400")
        @JsonProperty("start")
        val start: Long,

        @ApiModelProperty(required = true, value = "End time in seconds from midnight", position = 3, example = "83100")
        @JsonProperty("end")
        val end: Long,

        @ApiModelProperty(required = true, value = "Type of shift the detail relates to", position = 4, example = "OVERTIME")
        @JsonProperty("shiftType")
        val shiftType: ShiftType,

        @ApiModelProperty(required = true, value = "Detail activity", position = 5, example = "Canteen watch")
        @JsonProperty("activity")
        val activity: String
) {
        companion object {

                fun from(it: Detail): DetailDto {
                        return DetailDto(
                                it.date,
                                it.startTimeInSeconds,
                                it.endTimeInSeconds,
                                ShiftType.from(it.shiftType),
                                it.activity
                        )
                }

                fun from(details: Collection<Detail>): Collection<DetailDto> {
                        return details.map { from(it) }
                }

        }
}
