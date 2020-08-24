package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.model.DetailNotification
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import java.time.*

class NotificationDtoTest {

    @Test
    fun `Create Notification Dto from collection of ShiftNotification`() {
        val shifts = listOf(getValidShiftNotification())
        val notificationDtos = NotificationDto.fromShift(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos.first()
        Assertions.assertThat(first.quantumId).isEqualTo("XYZ")
        Assertions.assertThat(first.task).isEqualTo(null)
    }

    @Test
    fun `Create Notification Dto from collection of ShiftDetail`() {
        val shifts = listOf(getValidShiftDetail())
        val notificationDtos = NotificationDto.fromDetail(shifts)

        Assertions.assertThat(notificationDtos).hasSize(1)

        val first = notificationDtos.first()
        Assertions.assertThat(first.quantumId).isEqualTo("ABC")
        Assertions.assertThat(first.task).isEqualTo("Diving")
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        fun getValidShiftNotification(): ShiftNotification {
            val shiftDate = LocalDateTime.now(clock)

            val quantumId = "XYZ"
            val staffId = 7
            val shiftModified = shiftDate.minusDays(3)
            val shiftType = 0
            val actionType = 2

            return ShiftNotification(
                    quantumId,
                    shiftDate,
                    staffId,
                    shiftDate.toLocalDate(),
                    shiftModified,
                    shiftModified.toEpochSecond(ZoneOffset.of("Z")),
                    shiftType,
                    actionType

            )
        }

        fun getValidShiftDetail(): DetailNotification {
            val shiftDate = LocalDateTime.now(clock)

            val quantumId = "ABC"
            val staffId = 3
            val shiftModified = shiftDate.minusDays(3)
            val taskStart = 123L
            val taskEnd = 456L
            val task = "Diving"
            val shiftType = 1

            return DetailNotification(
                    quantumId,
                    staffId,
                    shiftDate.toLocalDate(),
                    shiftModified,
                    shiftModified.toEpochSecond(ZoneOffset.of("Z")),
                    taskStart,
                    taskEnd,
                    task,
                    shiftType
            )
        }
    }
}