package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ShiftOvertimeTest {

    @Test
    fun `Should return a valid ShiftDetail`() {

        val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        val dateWithTimeStamp: LocalDateTime = LocalDateTime.now(clock)
        val date: LocalDate = dateWithTimeStamp.toLocalDate()
        val staffId = 1823
        val detailStartTimeInSeconds = 7200L
        val detailEndTimeInSeconds = 84500L
        val task = "Phone Center"


        val overtime = ShiftOvertime(
                date,
                dateWithTimeStamp,
                staffId,
                detailStartTimeInSeconds,
                detailEndTimeInSeconds,
                task
        )
        Assertions.assertThat(overtime.date).isEqualTo(date)
        Assertions.assertThat(overtime.dateTimeStamp).isEqualTo(dateWithTimeStamp)
        Assertions.assertThat(overtime.detailStartTimeInSeconds).isEqualTo(detailStartTimeInSeconds)
        Assertions.assertThat(overtime.detailEndTimeInSeconds).isEqualTo(detailEndTimeInSeconds)
        Assertions.assertThat(overtime.task).isEqualTo(task)
    }
}

