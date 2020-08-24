package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class OvertimesDtoTestDetail {
    @Test
    fun `Create Overtimes Dto from collection of ShiftOvertime`() {
        val overtimes = listOf(getValidShiftOvertime())
        val overtimesDto = DetailsDto.from(overtimes)
        val first = overtimesDto.details.first()


        Assertions.assertThat(first.date).isEqualTo(date)
        Assertions.assertThat(first.start).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(first.end).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(first.activity).isEqualTo(activity)
        Assertions.assertThat(first.type).isEqualTo(ShiftType.OVERTIME.name)
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val date = LocalDate.now(clock)
        val detailStartTimeInSeconds = 7200L
        val detailEndTimeInSeconds = 84500L
        val activity = "Phone Center"

        fun getValidShiftOvertime(): Detail {

            return Detail(
                    date,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    activity
            )
        }
    }
}