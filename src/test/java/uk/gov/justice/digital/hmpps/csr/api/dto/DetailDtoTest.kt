package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DetailDtoTest {

  @Test
  fun `Create Overtime Dto`() {
    val detail = getFullyPopulatedDetail()
    val start = LocalDate.now(clock).atTime(LocalTime.MIN)
    val end = LocalDate.now(clock).atTime(LocalTime.parse("23:59"))
    val detailDto = DetailDto.from(detail)

    assertThat(detailDto.quantumId).isEqualTo(quantumId)
    assertThat(detailDto.shiftModified).isEqualTo(shiftModified)
    assertThat(detailDto.shiftType).isEqualTo(shiftType)
    assertThat(detailDto.detailStart).isEqualTo(start)
    assertThat(detailDto.detailEnd).isEqualTo(end)
    assertThat(detailDto.activity).isEqualTo(activity)
    assertThat(detailDto.actionType).isEqualTo(actionType)
  }

  @Test
  fun `Create Overtime Dto even if values are null`() {
    val detail = Detail(
      null,
      null,
      LocalDate.now(clock),
      ShiftType.SHIFT.value,
      null,
      null,
      null,
      null,
      null
    )
    val start = LocalDate.now(clock).atTime(LocalTime.MIN)
    val detailDto = DetailDto.from(detail)

    assertThat(detailDto.quantumId).isNull()
    assertThat(detailDto.shiftModified).isNull()
    assertThat(detailDto.detailStart).isEqualTo(start)
    assertThat(detailDto.detailEnd).isEqualTo(start)
    assertThat(detailDto.activity).isNull()
    assertThat(detailDto.actionType).isNull()
  }

  @Test
  fun `Create Dto from Shift CmdNotification`() {
    val detail = CmdNotification(
      id = 101,
      staffId = 5,
      levelId = 1000,
      onDate = LocalDate.parse("2022-04-01"),
      quantumId = "A-USER",
      lastModified = LocalDateTime.parse("2022-03-30T15:23:00"),
      actionType = 47015,
      startTimeInSeconds = 3600,
      endTimeInSeconds = 7200,
      activity = null,
    )

    val detailDto = DetailDto.from(detail)

    assertThat(detailDto).isEqualTo(
      DetailDto(
        id = 101,
        quantumId = "A-USER",
        shiftModified = LocalDateTime.parse("2022-03-30T15:23:00"),
        shiftType = ShiftType.SHIFT,
        detailStart = LocalDateTime.parse("2022-04-01T01:00:00"),
        detailEnd = LocalDateTime.parse("2022-04-01T02:00:00"),
        activity = null,
        actionType = ActionType.ADD,
      )
    )
  }

  @Test
  fun `Create Dto from Detail CmdNotification`() {
    val detail = CmdNotification(
      id = 101,
      staffId = 5,
      levelId = 4000,
      onDate = LocalDate.parse("2022-04-01"),
      quantumId = "A-USER",
      lastModified = LocalDateTime.parse("2022-03-30T15:23:00"),
      actionType = null,
      startTimeInSeconds = 3600,
      endTimeInSeconds = 7200,
      activity = "CCTV Monitoring",
    )

    val detailDto = DetailDto.from(detail)

    assertThat(detailDto).isEqualTo(
      DetailDto(
        id = 101,
        quantumId = "A-USER",
        shiftModified = LocalDateTime.parse("2022-03-30T15:23:00"),
        shiftType = ShiftType.OVERTIME,
        detailStart = LocalDateTime.parse("2022-04-01T01:00:00"),
        detailEnd = LocalDateTime.parse("2022-04-01T02:00:00"),
        activity = "CCTV Monitoring",
        actionType = ActionType.EDIT,
      )
    )
  }

  companion object {
    private val clock =
      Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

    private val shiftDate: LocalDate = LocalDate.now(clock)
    private const val detailStartTimeInSeconds = 0L
    private const val detailEndTimeInSeconds = 23 * 60 * 60 + 59 * 60L

    const val quantumId = "XYZ"
    val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
    val shiftType = ShiftType.OVERTIME
    val actionType = ActionType.EDIT
    const val activity = "Phone Center"

    fun getFullyPopulatedDetail(): Detail {

      return Detail(
        quantumId,
        shiftModified,
        shiftDate,
        shiftType.value,
        detailStartTimeInSeconds,
        detailEndTimeInSeconds,
        activity,
        actionType.value,
        null
      )
    }
  }
}
