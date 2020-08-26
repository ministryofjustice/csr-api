package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DetailNotificationTest {

    @Test
    fun `Should return a valid DetailNotification`() {

        val date = LocalDateTime.now(clock)

        val quantumId = "ABC"
        val shiftDate = date.toLocalDate()
        val shiftModified = date.minusDays(3)
        val taskStart = 123L
        val taskEnd = 456L
        val task = "Diving"
        val shiftType = ShiftType.OVERTIME.value

        val detail = DetailNotification(
                quantumId,
                shiftDate,
                shiftModified,
                taskStart,
                taskEnd,
                task,
                shiftType
        )

        Assertions.assertThat(detail.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(detail.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(detail.lastModified).isEqualTo(shiftModified)
        Assertions.assertThat(detail.detailStartTimeInSeconds).isEqualTo(taskStart)
        Assertions.assertThat(detail.detailEndTimeInSeconds).isEqualTo(taskEnd)
        Assertions.assertThat(detail.task).isEqualTo(task)
        Assertions.assertThat(detail.shiftType).isEqualTo(shiftType)

    }
        companion object {

            val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        }
    }