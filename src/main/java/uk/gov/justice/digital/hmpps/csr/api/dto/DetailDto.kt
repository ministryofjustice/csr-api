package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.LocalDateTime

data class DetailDto @JsonCreator constructor(
  @field:Schema(description = "User unique identifier", example = "AB102CD")
  @JsonProperty("quantumId")
  val quantumId: String?,

  @field:Schema(description = "Date shift was last modified", example = "2020-08-22T09:45:55")
  @JsonProperty("shiftModified")
  val shiftModified: LocalDateTime?,

  @field:Schema(description = "Type of shift the detail relates to", example = "OVERTIME")
  @JsonProperty("shiftType")
  val shiftType: ShiftType,

  @field:Schema(description = "Detail start date time", example = "2020-08-22T09:15:00")
  @JsonProperty("detailStart")
  val detailStart: LocalDateTime,

  @field:Schema(description = "Detail end date time", example = "2020-08-22T09:15:00")
  @JsonProperty("detailEnd")
  val detailEnd: LocalDateTime,

  @field:Schema(description = "Detail activity", example = "Canteen watch")
  @JsonProperty("activity")
  val activity: String?,

  @field:Schema(description = "Type of modification action", example = "EDIT")
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
        detail.actionType?.let { type -> ActionType.from(type) }
      )
    }
  }
}
