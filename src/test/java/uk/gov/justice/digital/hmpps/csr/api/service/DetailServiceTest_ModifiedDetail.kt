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
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.repository.DetailRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class DetailServiceTest_ModifiedDetail {
    private val detailRepository: DetailRepository = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val service = DetailService(
            detailRepository,
            authenticationFacade
    )

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(detailRepository)
        clearMocks(authenticationFacade)
    }

    @Nested
    @DisplayName("Get Notification tests")
    inner class GetNotificationTests {

        @Test
        fun `Should get Shift Notifications`() {
            val planUnit = "ABC"

            val notifications = listOf(getValidShiftDetail())
            every { detailRepository.getModifiedShifts(planUnit) } returns notifications
            every { detailRepository.getModifiedDetails(planUnit) } returns listOf()

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedShifts(planUnit) }

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get Shift Detail Notifications`() {
            val planUnit = "ABC"

            val notifications = listOf(getValidShiftDetail())
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns notifications

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should combine Shift and Shift Detail Notifications`() {
            val planUnit = "ABC"

            val shiftNotifications = listOf(getValidShiftDetail())
            val detailNotifications = listOf(getValidShiftDetail())

            every { detailRepository.getModifiedShifts(planUnit) } returns shiftNotifications
            every { detailRepository.getModifiedDetails(planUnit) } returns detailNotifications

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(2)
        }
    }

    companion object {
        private fun getValidShiftDetail(): Detail {

            val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "XYZ"
            val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
            val shiftDate: LocalDate = LocalDate.now(clock)
            val shiftType = ShiftType.OVERTIME
            val actionType = ActionType.EDIT
            val detailStartTimeInSeconds = 7200L
            val detailEndTimeInSeconds = 84500L
            val activity = "Phone Center"

            return Detail(
                    quantumId,
                    shiftModified,
                    shiftDate,
                    shiftType.value,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    activity,
                    actionType.value
            )
        }

    }
}