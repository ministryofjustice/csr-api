package uk.gov.justice.digital.hmpps.csr.api.service

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.repository.DetailRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Detail Service tests")
internal class DetailServiceTest {
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
    @DisplayName("Get Service tests")
    inner class GetServiceTests {

        @Test
        fun `Should get Details`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, 456L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
        }

        @Test
        fun `Should get empty Details`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            every { detailRepository.getDetails(from, to, quantumId) } returns listOf()
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(0)
        }

    }

    @Nested
    @DisplayName("Service Task Time tests")
    inner class ServiceTaskTimeTests {

        @Test
        fun `Should subtract a day when start time less than 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-1234L, 456L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().date).isEqualTo(LocalDate.now(clock).minusDays(1))
        }

        @Test
        fun `Should replace full_day start time with 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-2147483648L, 456L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().start).isEqualTo(0)
        }

        @Test
        fun `Should replace full_day end time with 0`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, -2147483648L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().end).isEqualTo(0)
        }

        @Test
        fun `Should add 24H to less than 0 start time`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(-123L, 456L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().start).isEqualTo(86277)
        }

        @Test
        fun `Should add 24H to less than 0 end time`() {
            val quantumId = "XYZ"
            val from = LocalDate.now(clock).minusDays(1)
            val to = LocalDate.now(clock).plusDays(1)

            val details = listOf(getValidShiftDetail(123L, -456L))
            every { detailRepository.getDetails(from, to, quantumId) } returns details
            every { authenticationFacade.currentUsername } returns quantumId

            val returnValue = service.getStaffDetails(from, to, quantumId)

            verify { detailRepository.getDetails(from, to, quantumId) }
            confirmVerified(detailRepository)

            assertThat(returnValue).hasSize(1)
            assertThat(returnValue.first().end).isEqualTo(85944)
        }
    }

    companion object {
        private val clock = Clock.fixed(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())


        private fun getValidShiftDetail(start: Long, end: Long): Detail {

            val date = LocalDateTime.now(clock).toLocalDate()
            val shiftType = ShiftType.SHIFT.value
            val activity = "Bed Watch"

            return Detail(
                    date,
                    start,
                    end,
                    shiftType,
                    activity
            )
        }

    }
}