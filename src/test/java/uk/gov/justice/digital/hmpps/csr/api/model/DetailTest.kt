package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class DetailTest {

    @Test
    fun `Should return a valid ShiftDetail`() {

        val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val date: LocalDate = LocalDate.now(clock)
        val detailStartTimeInSeconds = 7200L
        val detailEndTimeInSeconds = 84500L
        val activity = "Phone Center"


        val overtime = Detail(
                date,
                detailStartTimeInSeconds,
                detailEndTimeInSeconds,
                activity
        )
        Assertions.assertThat(overtime.date).isEqualTo(date)
        Assertions.assertThat(overtime.detailStartTimeInSeconds).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(overtime.detailEndTimeInSeconds).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(overtime.activity).isEqualTo(activity)
    }
}

