package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "NotificationsDto")
data class NotificationsDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "List of notifications", position = 1, dataType = "List")
        @JsonProperty("notifications")
        var details: Collection<NotificationDto>
) {
        companion object {

                fun from(notifications: Collection<NotificationDto>): NotificationsDto {
                        return NotificationsDto(notifications)
                }

        }
}


