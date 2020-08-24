package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.csr.api.model.Detail

@ApiModel(description = "DetailsDto")
data class DetailsDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "List of details", position = 1, dataType = "List")
        @JsonProperty("details")
        var details: Collection<DetailDto>
) {
        companion object {

                fun from(details: Collection<Detail>): DetailsDto {
                        return DetailsDto(DetailDto.from(details))
                }

        }
}


