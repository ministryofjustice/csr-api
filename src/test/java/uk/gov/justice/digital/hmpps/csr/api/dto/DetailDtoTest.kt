package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class DetailDtoTest {

    @Test
    fun `Create Overtime Dto`() {
        val overtime = getValidShiftOvertime()
        val overtimeDto = DetailDto.from(overtime)

        Assertions.assertThat(overtimeDto.date).isEqualTo(date)
        Assertions.assertThat(overtimeDto.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(overtimeDto.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(overtimeDto.shiftType).isEqualTo(ShiftType.OVERTIME)
        Assertions.assertThat(overtimeDto.activity).isEqualTo(activity)
    }

    @Test
    fun `Create Overtime Dto from collection`() {
        val overtimes = listOf(getValidShiftOvertime())
        val overtimesDto = DetailDto.from(overtimes)
        val first = overtimesDto.first()


        Assertions.assertThat(first.date).isEqualTo(date)
        Assertions.assertThat(first.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(first.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(first.shiftType).isEqualTo(ShiftType.OVERTIME)
        Assertions.assertThat(first.activity).isEqualTo(activity)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val date: LocalDate = LocalDate.now(clock)
        const val detailStartTimeInSeconds = 7200L
        const val detailEndTimeInSeconds = 84500L
        val shiftType = ShiftType.OVERTIME.value
        const val activity = "Phone Center"

        fun getValidShiftOvertime(): Detail {

            return Detail(
                    date,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    shiftType,
                    activity
            )
        }
    }
}