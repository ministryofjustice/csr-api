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
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Detail Service tests")
internal class DetailServiceTest {
    private val sqlRepository: SqlRepository = mockk(relaxUnitFun = true)
    private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
    private val service = DetailService(
            sqlRepository,
            authenticationFacade
    )

    @BeforeEach
    fun resetAllMocks() {
        clearMocks(sqlRepository)
        clearMocks(authenticationFacade)
    }

    @Nested
    @DisplayName("Get Service tests")
    inner class GetServiceTests {

        @Test
        fun `Should get Details`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get empty Details`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            every { sqlRepository.getDetails(from, to, quantumId) } returns listOf()
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(0)
        }

    }

    @Nested
    @DisplayName("Service Task Time tests")
    inner class ServiceTaskTimeTests {

        @Test
        fun `Should subtract time when start time less than 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-1234L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(clock).atStartOfDay().minusSeconds(1234))
        }

        @Test
        fun `Should replace full_day start time with 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-2147483648L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(clock).atStartOfDay())
        }

        @Test
        fun `Should replace full_day end time with 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, -2147483648L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(clock).atStartOfDay().minusSeconds(0))
        }

        @Test
        fun `Should replace start time of 86400 with time plus 86400`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(86400L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(clock).atStartOfDay().plusSeconds(86400))
        }

        @Test
        fun `Should replace end time of 86400 with time plus 86400`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, 86400L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(clock).atStartOfDay().plusSeconds(86400))
        }

        @Test
        fun `Should replace start time of 86401 with time minus 86400`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(86401L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(clock).atStartOfDay().plusSeconds(86401))
        }

        @Test
        fun `Should replace end time of 86401 with time minus 86400`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, 86401L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(clock).atStartOfDay().plusSeconds(86401))
        }

        @Test
        fun `Should add 24H to less than 0 start time`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-123L, 456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailStart).isEqualTo(LocalDate.now(clock).atStartOfDay().minusSeconds(123))
        }

        @Test
        fun `Should add 24H to less than 0 end time`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, -456L))
            every { sqlRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { sqlRepository.getDetails(from, to, quantumId) }
            confirmVerified(sqlRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().detailEnd).isEqualTo(LocalDate.now(clock).atStartOfDay().minusSeconds(456))
        }
    }

    companion object {
        val clock: Clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

        private fun getValidShiftDetail(start: Long, end: Long): Detail {

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