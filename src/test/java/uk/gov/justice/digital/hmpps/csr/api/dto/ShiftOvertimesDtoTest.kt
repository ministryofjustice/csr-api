package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftOvertime
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ShiftOvertimesDtoTest {
    @Test
    fun `Create Overtimes Dto from collection of ShiftOvertime`() {
        val overtimes = listOf(getValidShiftOvertime())
        val overtimesDto = ShiftOvertimesDto.from(overtimes)
        val first = overtimesDto.details.first()


        Assertions.assertThat(first.date).isEqualTo(date)
        Assertions.assertThat(first.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(first.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(first.task).isEqualTo(task)
        Assertions.assertThat(first.type).isEqualTo(ShiftType.OVERTIME.name)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val dateWithTimeStamp = LocalDateTime.now(clock)
        val date = dateWithTimeStamp.toLocalDate()
        val staffId = 1823
        val detailStartTimeInSeconds = 7200L
        val detailEndTimeInSeconds = 84500L
        val task = "Phone Center"

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