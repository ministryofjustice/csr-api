package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftOvertime
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ShiftOvertimeDtoTest {

    @Test
    fun `Create Overtime Dto from ShiftOvertime`() {
        val overtime = getValidShiftOvertime()
        val overtimeDto = ShiftOvertimeDto.from(overtime)

        Assertions.assertThat(overtimeDto.date).isEqualTo(date)
        Assertions.assertThat(overtimeDto.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(overtimeDto.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(overtimeDto.task).isEqualTo(task)
        Assertions.assertThat(overtimeDto.type).isEqualTo(ShiftType.OVERTIME.name)
    }

    @Test
    fun `Create Overtime Dto from collection of ShiftOvertime`() {
        val overtimes = listOf(getValidShiftOvertime())
        val overtimesDto = ShiftOvertimeDto.from(overtimes)
        val first = overtimesDto.first()


        Assertions.assertThat(first.date).isEqualTo(date)
        Assertions.assertThat(first.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(first.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(first.task).isEqualTo(task)
        Assertions.assertThat(first.type).isEqualTo(ShiftType.OVERTIME.name)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        private val dateWithTimeStamp: LocalDateTime = LocalDateTime.now(clock)
        val date: LocalDate = dateWithTimeStamp.toLocalDate()
        private const val staffId = 1823
        const val detailStartTimeInSeconds = 7200L
        const val detailEndTimeInSeconds = 84500L
        const val task = "Phone Center"

        fun getValidShiftOvertime(): ShiftOvertime {

            return ShiftOvertime(
                    date,
                    dateWithTimeStamp,
                    staffId,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    task
            )
        }
    }
}