package uk.gov.justice.digital.hmpps.csr.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.DetailType
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
            verify { detailRepository.getModifiedDetails(planUnit) }

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

    @Nested
    @DisplayName("Service Task Time tests")
    inner class ServiceTaskTimeTests {

        @Test
        fun `Should subtract time when start time less than 0`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(-1234L, 456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().minusSeconds(1234))
        }

        @Test
        fun `Should replace full_day start time with 0`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(-2147483648L, 456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(0))
        }

        @Test
        fun `Should replace full_day end time with 0`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(123L, -2147483648L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(0))
        }

        @Test
        fun `Should replace start time of 86400 with time plus 86400`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(86400L, 456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(86400))
        }

        @Test
        fun `Should replace end time of 86400 with time minus 86400`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(123L, 86400L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(86400))
        }

        @Test
        fun `Should replace start time of 86401 with time minus 86400`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(86401L, 456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(86401))
        }

        @Test
        fun `Should replace end time of 86401 with time minus 86400`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(123L, 86401L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().plusSeconds(86401))
        }

        @Test
        fun `Should add 24H to less than 0 start time`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(-123L, 456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().minusSeconds(123))
        }

        @Test
        fun `Should add 24H to less than 0 end time`() {
            val planUnit = "ABC"

            val details = listOf(getValidShiftDetail(123L, -456L))
            every { detailRepository.getModifiedShifts(planUnit) } returns listOf()
            every { detailRepository.getModifiedDetails(planUnit) } returns details

            val returnValue = service.getModifiedDetailByPlanUnit(planUnit)

            verify { detailRepository.getModifiedShifts(planUnit) }
            verify { detailRepository.getModifiedDetails(planUnit) }

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(DetailServiceTest.clock).atStartOfDay().minusSeconds(456))
        }
    }

    companion object {
        private fun getValidShiftDetail(start: Long = 7200L, end: Long = 84500L): Detail {

            val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
            val quantumId = "XYZ"
            val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
            val shiftDate: LocalDate = LocalDate.now(clock)
            val shiftType = ShiftType.OVERTIME
            val actionType = ActionType.EDIT
            val detailType = DetailType.UNSPECIFIC
            val activity = "Phone Center"

            return Detail(
                    quantumId,
                    shiftModified,
                    shiftDate,
                    shiftType.value,
                    start,
                    end,
                    activity,
                    detailType.value,
                    actionType.value
            )
        }

    }
}