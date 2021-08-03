package uk.gov.justice.digital.hmpps.csr.api.service

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
@DisplayName("Notification Service tests")
internal class DetailServiceTest_ModifiedDetail {
  private val sqlRepository: SqlRepository = mockk(relaxUnitFun = true)
  private val authenticationFacade: AuthenticationFacade = mockk(relaxUnitFun = true)
  private val service = DetailService(
    sqlRepository,
    authenticationFacade
  )

  private val clock: Clock =
    Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val planUnit = "ABC"
  private val shiftDate: LocalDate = LocalDate.now(clock)

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(sqlRepository)
    clearMocks(authenticationFacade)
  }

  @AfterEach
  fun confirmVerified() {
    confirmVerified(sqlRepository)
    confirmVerified(authenticationFacade)
  }

  @Nested
  @DisplayName("Get Modified Detail")
  inner class GetModifiedDetailTests {

    @Test
    fun `Should get Modified Shifts`() {
      val notifications = listOf(getValidDetail())
      every { sqlRepository.getModifiedShifts(planUnit) } returns notifications
      every { sqlRepository.getModifiedDetails(planUnit) } returns listOf()

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should get Modified Details`() {
      val notifications = listOf(getValidDetail())
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns notifications

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should combine Shifts and Details`() {
      val shiftDetails = listOf(getValidDetail())
      val detailDetails = listOf(getValidDetail())
      every { sqlRepository.getModifiedShifts(planUnit) } returns shiftDetails
      every { sqlRepository.getModifiedDetails(planUnit) } returns detailDetails

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(2)
    }
  }

  @Nested
  @DisplayName("Service Detail Time tests")
  inner class ServiceDetailTimeTests {

    @Test
    fun `Should subtract time when start time less than 0`() {
      val details = listOf(getValidDetail(-1234L, 456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().minusSeconds(1234))
    }

    @Test
    fun `Should replace start full day magic number with 0`() {
      val details = listOf(getValidDetail(-2147483648L, 456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().plusSeconds(0))
    }

    @Test
    fun `Should replace end full day magic number with 0`() {
      val details = listOf(getValidDetail(123L, -2147483648L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().plusSeconds(0))
    }

    @Test
    fun `Should add start time of 86401`() {
      val details = listOf(getValidDetail(86401L, 456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().plusSeconds(86401))
    }

    @Test
    fun `Should replace end time of 86400 with time minus 0`() {
      val details = listOf(getValidDetail(123L, 86400L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().plusSeconds(0))
    }

    @Test
    fun `Should add end time of 86401`() {
      val details = listOf(getValidDetail(86401L, 456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().plusSeconds(86401))
    }

    @Test
    fun `Should replace end time of 86401 with time minus 86400`() {
      val details = listOf(getValidDetail(123L, 86401L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().plusSeconds(86401))
    }

    @Test
    fun `Should subtract less than 0 start time`() {
      val details = listOf(getValidDetail(-123L, 456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().minusSeconds(123))
    }

    @Test
    fun `Should subtract less than 0 end time`() {
      val details = listOf(getValidDetail(123L, -456L))
      every { sqlRepository.getModifiedShifts(planUnit) } returns listOf()
      every { sqlRepository.getModifiedDetails(planUnit) } returns details

      val returnValue = service.getModifiedDetailsByPlanUnit(planUnit)

      verify { sqlRepository.getModifiedShifts(planUnit) }
      verify { sqlRepository.getModifiedDetails(planUnit) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().minusSeconds(456))
    }
  }

  private fun getValidDetail(start: Long = 7200L, end: Long = 84500L): Detail {

    val clock =
      Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    val quantumId = "XYZ"
    val shiftDate: LocalDate = LocalDate.now(clock)
    val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
    val shiftType = ShiftType.OVERTIME
    val actionType = ActionType.EDIT
    val activity = "Phone Center"

    return Detail(
      quantumId,
      shiftModified,
      shiftDate,
      shiftType.value,
      start,
      end,
      activity,
      actionType.value,
      null
    )
  }
}
