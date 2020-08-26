package uk.gov.justice.digital.hmpps.csr.api.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.csr.api.model.DetailNotification
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.csr.api.repository.NotificationRepository
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class NotificationServiceTest {
    private val notificationRepository: NotificationRepository = mockk(relaxUnitFun = true)
    private val service = NotificationService(notificationRepository)

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(notificationRepository)
    }

    @Nested
    @DisplayName("Get Notification tests")
    inner class GetNotificationTests {

        @Test
        fun `Should get Shift Notifications`() {
            val planUnit = "ABC"

            val notifications = listOf(getValidShiftNotification())
            every { notificationRepository.getModifiedShifts(planUnit) } returns notifications
            every { notificationRepository.getModifiedDetail(planUnit) } returns listOf()

            val returnValue = service.getShiftAndDetailNotifications(planUnit)

            verify { notificationRepository.getModifiedShifts(planUnit) }
            verify { notificationRepository.getModifiedDetail(planUnit) }

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Shift Detail Notifications`() {
            val planUnit = "ABC"

            val notifications = listOf(getValidDetailNotification())
            every { notificationRepository.getModifiedShifts(planUnit) } returns listOf()
            every { notificationRepository.getModifiedDetail(planUnit) } returns notifications

            val returnValue = service.getShiftAndDetailNotifications(planUnit)

            verify { notificationRepository.getModifiedShifts(planUnit) }
            verify { notificationRepository.getModifiedDetail(planUnit) }

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should combine Shift and Shift Detail Notifications`() {
            val planUnit = "ABC"

            val shiftNotifications = listOf(getValidShiftNotification())
            val detailNotifications = listOf(getValidDetailNotification())

            every { notificationRepository.getModifiedShifts(planUnit) } returns shiftNotifications
            every { notificationRepository.getModifiedDetail(planUnit) } returns detailNotifications

            val returnValue = service.getShiftAndDetailNotifications(planUnit)

            verify { notificationRepository.getModifiedShifts(planUnit) }
            verify { notificationRepository.getModifiedDetail(planUnit) }

            assertThat(returnValue).hasSize(2)
        }
    }

    companion object {
        fun getValidShiftNotification(): ShiftNotification {

            val quantumId = "XYZ"
            val shiftDate = LocalDate.now().plusDays(1)
            val lastModified = LocalDateTime.now()
            val shiftType = 0
            val actionType = 0

            return ShiftNotification(
                    quantumId,
                    shiftDate,
                    lastModified,
                    shiftType,
                    actionType
            )
        }

        fun getValidDetailNotification(): DetailNotification {

            val quantumId = "XYZ"
            val shiftDate = LocalDate.now().plusDays(1)
            val lastModified = LocalDateTime.now()
            val start = 12345L
            val end = 54321L
            val activity = "Bed Watch"
            val shiftType = 0

            return DetailNotification(
                    quantumId,
                    shiftDate,
                    lastModified,
                    start,
                    end,
                    activity,
                    shiftType
            )
        }

    }
}