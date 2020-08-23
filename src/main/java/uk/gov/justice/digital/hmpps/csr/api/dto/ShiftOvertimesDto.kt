package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftOvertime

@ApiModel(description = "ShiftOvertimeDto")
data class ShiftOvertimesDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "List of overtime details", position = 1, dataType = "List")
        @JsonProperty("details")
        var details: Collection<ShiftOvertimeDto>
) {
        companion object {

                fun from(overtimes: Collection<ShiftOvertime>): ShiftOvertimesDto {
                        return ShiftOvertimesDto(ShiftOvertimeDto.from(overtimes))
                }

        }
}


