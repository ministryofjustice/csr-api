package uk.gov.justice.digital.hmpps.csr.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.LocalDate
import java.time.LocalDateTime

data class
DetailDto @JsonCreator constructor(
  @field:Schema(description = "Unique identifier in polling table")
  val id: Long? = null,

  @field:Schema(description = "User unique identifier", example = "AB102CD")
  val quantumId: String?,

  @field:Schema(description = "Date time the shift was last modified", example = "2020-08-22T09:45:55")
  val shiftModified: LocalDateTime?,

  @field:Schema(description = "Type of shift the detail relates to", example = "OVERTIME")
  val shiftType: ShiftType,

  @field:Schema(description = "Detail start date time", example = "2020-08-22T09:15:00")
  val detailStart: LocalDateTime,

  @field:Schema(description = "Detail end date time", example = "2020-08-22T09:15:00")
  val detailEnd: LocalDateTime,

  @field:Schema(description = "Detail activity", example = "Canteen watch")
  val activity: String?,

  @field:Schema(description = "Type of modification action", example = "EDIT")
  val actionType: ActionType?,

) {
  companion object {

    fun from(detail: Detail): DetailDto = DetailDto(
      id = null,
      quantumId = detail.quantumId,
      shiftModified = detail.shiftModified,
      shiftType = ShiftType.from(detail.shiftType),

      // We don't care about the shiftDate on its own
      // We want to include it in the detail's start/end values
      // So that our clients don't have to work it out themselves
      detailStart = calculateDetailDateTime(detail.shiftDate, detail.startTimeInSeconds ?: 0L),
      detailEnd = calculateDetailDateTime(detail.shiftDate, detail.endTimeInSeconds ?: 0L),

      activity = detail.activity,
      actionType = detail.actionType?.let { type -> ActionType.from(type) },
    )

    fun from(detail: CmdNotification): DetailDto = DetailDto(
      id = detail.id,
      quantumId = detail.quantumId,
      shiftModified = detail.lastModified,
      shiftType = if (detail.levelId == 4000) ShiftType.OVERTIME else ShiftType.SHIFT,

      // We don't care about the shiftDate on its own
      // We want to include it in the detail's start/end values
      // So that our clients don't have to work it out themselves
      detailStart = calculateDetailDateTime(detail.onDate, detail.startTimeInSeconds ?: 0L),
      detailEnd = calculateDetailDateTime(detail.onDate, detail.endTimeInSeconds ?: 0L),

      activity = detail.activity,
      actionType = detail.actionType.let {
        when (it) {
          47012 -> ActionType.DELETE
          0, 47001 -> ActionType.EDIT
          47006, 47015 -> ActionType.ADD
          else -> ActionType.UNCHANGED
        }
      },
    )

    // if both start and end are this magic number then detail is a full day activity
    private const val FULL_DAY_ACTIVITY = -2_147_483_648L

    /*
     CSR database uses positive or negative numbers to offset the shiftDate.
     e.g. 04/09/2020T00:00:00 with a detail start of -10 is actually 03/09/2020T23:59:50
     */
    private fun calculateDetailDateTime(shiftDate: LocalDate, detailTime: Long): LocalDateTime {
      val normalisedTime = if (detailTime == 86400L) {
        0
      } else {
        detailTime
      }

      return if (normalisedTime != FULL_DAY_ACTIVITY) {
        // plusSeconds allows negative numbers.
        shiftDate.atStartOfDay().plusSeconds(normalisedTime)
      } else {
        shiftDate.atStartOfDay()
      }
    }
  }
}
