package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftNotificationDtoTest
import java.time.*

class ShiftDetailTest {

    @Test
    fun `Should return a valid ShiftDetail`() {

        val date = LocalDateTime.now(clock)

        val quantumId = "ABC"
        val staffId = 3
        val shiftDate = date.toLocalDate()
        val shiftModified = date.minusDays(3)
        val shiftModifiedInSeconds = shiftModified.toEpochSecond(ZoneOffset.of("Z"))
        val taskStart = 123L
        val taskEnd = 456L
        val task = "Diving"
        val shiftType = 1

        val detail = ShiftDetail(
                quantumId,
                staffId,
                shiftDate,
                shiftModified,
                shiftModifiedInSeconds,
                taskStart,
                taskEnd,
                task,
                shiftType
        )

        Assertions.assertThat(detail.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(detail.staffId).isEqualTo(staffId)
        Assertions.assertThat(detail.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(detail.lastModified).isEqualTo(shiftModified)
        Assertions.assertThat(detail.detailModifiedInSeconds).isEqualTo(shiftModifiedInSeconds)
        Assertions.assertThat(detail.detailStartTimeInSeconds).isEqualTo(taskStart)
        Assertions.assertThat(detail.detailEndTimeInSeconds).isEqualTo(taskEnd)
        Assertions.assertThat(detail.task).isEqualTo(task)
        Assertions.assertThat(detail.shiftType).isEqualTo(shiftType)

    }
        companion object {

            val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        }
    }