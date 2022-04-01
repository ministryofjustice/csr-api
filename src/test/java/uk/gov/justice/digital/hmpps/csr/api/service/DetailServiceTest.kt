package uk.gov.justice.digital.hmpps.csr.api.service

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
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
import uk.gov.justice.digital.hmpps.csr.api.model.DetailTemplate
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext
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

  private val clock: Clock =
    Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val quantumId = "XYZ"
  private val shiftDate: LocalDate = LocalDate.now(clock)
  private val from: LocalDate = shiftDate.minusDays(1)
  private val paddedFrom = from.minusDays(1)
  private val to: LocalDate = shiftDate.plusDays(1)

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(sqlRepository)
    clearMocks(authenticationFacade)

    every { authenticationFacade.currentUsername } returns quantumId
  }

  @AfterEach
  fun confirmVerified() {
    confirmVerified(sqlRepository)
    confirmVerified(authenticationFacade)
  }

  @Nested
  @DisplayName("Get Staff Details")
  inner class DetailServiceTests {

    @Test
    fun `Should pad 'from' dates by -1 days`() {
      val details = listOf(getValidShiftDetail(123L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
    }

    @Test
    fun `Should get Details`() {
      val details = listOf(getValidShiftDetail(123L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
    }

    @Test
    fun `Should get empty Details`() {
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns listOf()
      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(0)
    }

    @Test
    fun `Should merge details with template with 1 relative relation`() {
      val detailStart = 123L
      val detailEnd = 456L
      val templateStart = 1L
      val templateEnd = 2L
      val templateName = "TEMP01"
      val isRelative = true

      val details = listOf(getValidShiftDetailWithTemplateName(shiftDate, detailStart, detailEnd, templateName))
      val templates = listOf(getValidDetailTemplate(templateStart, templateEnd, isRelative, templateName))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName)) } returns templates

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName)) }

      val returnDetail = returnValue.elementAt(0)
      val calculatedStart = calculateDetailDateTime(shiftDate, detailStart + templateStart)
      val calculatedEnd = calculateDetailDateTime(shiftDate, detailStart + templateEnd)

      assertThat(returnValue).hasSize(1)
      assertThat(returnDetail.detailStart).isEqualTo(calculatedStart)
      assertThat(returnDetail.detailEnd).isEqualTo(calculatedEnd)
    }

    @Test
    fun `Should merge details with template with 1 non relative relation`() {
      val detailStart = 123L
      val detailEnd = 456L
      val templateStart = 1L
      val templateEnd = 2L
      val templateName = "TEMP01"
      val isRelative = false

      val details = listOf(getValidShiftDetailWithTemplateName(shiftDate, detailStart, detailEnd, templateName))
      val templates = listOf(getValidDetailTemplate(templateStart, templateEnd, isRelative, templateName))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName)) } returns templates

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName)) }

      val returnDetail = returnValue.elementAt(0)
      val calculatedStart = calculateDetailDateTime(shiftDate, templateStart)
      val calculatedEnd = calculateDetailDateTime(shiftDate, templateEnd)

      assertThat(returnValue).hasSize(1)
      assertThat(returnDetail.detailStart).isEqualTo(calculatedStart)
      assertThat(returnDetail.detailEnd).isEqualTo(calculatedEnd)
    }

    @Test
    fun `Should merge one detail with template of multiple entries`() {
      val detailStart = 123L
      val detailEnd = 456L
      val templateStart = 1L
      val templateEnd = 2L
      val templateName = "TEMP01"
      val isRelative = true

      val details = listOf(getValidShiftDetailWithTemplateName(shiftDate, detailStart, detailEnd, templateName))
      val templates = listOf(
        getValidDetailTemplate(templateStart, templateEnd, isRelative, templateName),
        getValidDetailTemplate(templateStart, templateEnd, isRelative, templateName),
        getValidDetailTemplate(templateStart, templateEnd, isRelative, templateName)
      )
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName)) } returns templates

      val returnValue = service.getStaffDetails(from, to, quantumId)
      val calculatedStart = calculateDetailDateTime(shiftDate, detailStart + templateStart)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName)) }

      assertThat(returnValue).hasSize(3)
      assertThat(returnValue.elementAt(0).detailStart).isEqualTo(calculatedStart)
      assertThat(returnValue.elementAt(1).detailStart).isEqualTo(calculatedStart)
      assertThat(returnValue.elementAt(2).detailStart).isEqualTo(calculatedStart)
    }

    @Test
    fun `Should merge one detail with template of multiple entries of varying isRelative value`() {
      val detailStart = 123L
      val detailEnd = 456L
      val templateStart = 1L
      val templateEnd = 2L
      val templateName = "TEMP01"

      val details = listOf(getValidShiftDetailWithTemplateName(shiftDate, detailStart, detailEnd, templateName))
      val templates = listOf(
        getValidDetailTemplate(templateStart, templateEnd, true, templateName),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName),
        getValidDetailTemplate(templateStart, templateEnd, true, templateName)
      )
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName)) } returns templates

      val returnValue = service.getStaffDetails(from, to, quantumId)
      val relativeStart = calculateDetailDateTime(shiftDate, detailStart + templateStart)
      val nonRelativeStart = calculateDetailDateTime(shiftDate, templateStart)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName)) }

      assertThat(returnValue).hasSize(4)
      assertThat(returnValue.elementAt(0).detailStart).isEqualTo(relativeStart)
      assertThat(returnValue.elementAt(1).detailStart).isEqualTo(nonRelativeStart)
      assertThat(returnValue.elementAt(2).detailStart).isEqualTo(nonRelativeStart)
      assertThat(returnValue.elementAt(3).detailStart).isEqualTo(relativeStart)
    }

    @Test
    fun `Should merge multiple details with templates of multiple entries`() {
      val detailStart1 = 123L
      val detailEnd1 = 456L
      val detailStart2 = 12L
      val detailEnd2 = 45L

      val templateStart = 1L
      val templateEnd = 2L
      val templateName1 = "TEMP01"
      val templateName2 = "TEMP02"

      val details = listOf(
        getValidShiftDetailWithTemplateName(shiftDate, detailStart1, detailEnd1, templateName1),
        getValidShiftDetailWithTemplateName(shiftDate, detailStart2, detailEnd2, templateName2)
      )
      val templates = listOf(
        getValidDetailTemplate(templateStart, templateEnd, true, templateName1),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName1),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName1),
        getValidDetailTemplate(templateStart, templateEnd, true, templateName1),
        getValidDetailTemplate(templateStart, templateEnd, true, templateName2),
        getValidDetailTemplate(templateStart, templateEnd, true, templateName2),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName2)
      )

      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName1, templateName2)) } returns templates

      val returnValue = service.getStaffDetails(from, to, quantumId)
      val relativeStart1 = calculateDetailDateTime(shiftDate, detailStart1 + templateStart)
      val relativeStart2 = calculateDetailDateTime(shiftDate, detailStart2 + templateStart)
      val nonRelativeStart = calculateDetailDateTime(shiftDate, templateStart)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName1, templateName2)) }

      assertThat(returnValue).hasSize(7)
      assertThat(returnValue.elementAt(0).detailStart).isEqualTo(relativeStart1)
      assertThat(returnValue.elementAt(1).detailStart).isEqualTo(nonRelativeStart)
      assertThat(returnValue.elementAt(2).detailStart).isEqualTo(nonRelativeStart)
      assertThat(returnValue.elementAt(3).detailStart).isEqualTo(relativeStart1)
      assertThat(returnValue.elementAt(4).detailStart).isEqualTo(relativeStart2)
      assertThat(returnValue.elementAt(5).detailStart).isEqualTo(relativeStart2)
      assertThat(returnValue.elementAt(6).detailStart).isEqualTo(nonRelativeStart)
    }

    @Test
    fun `Should merge multiple details with templates over multiple days`() {
      val day1 = shiftDate
      val day2 = shiftDate.plusDays(1)
      val day3 = shiftDate.plusDays(2)
      val detailStart1 = 100L
      val detailEnd1 = 200L
      val detailStart2 = 300L
      val detailEnd2 = 400L
      val detailStart3 = 500L
      val detailEnd3 = 600L

      val templateStart = 1L
      val templateEnd = 2L
      val templateName = "TEMP01"

      val details = listOf(
        getValidShiftDetailWithTemplateName(day1, detailStart1, detailEnd1, templateName),
        getValidShiftDetailWithTemplateName(day2, detailStart2, detailEnd2, templateName),
        getValidShiftDetailWithTemplateName(day3, detailStart3, detailEnd3, templateName)
      )
      val templates = listOf(
        getValidDetailTemplate(templateStart, templateEnd, true, templateName),
        getValidDetailTemplate(templateStart, templateEnd, false, templateName)
      )

      every { sqlRepository.getDetails(paddedFrom, to.plusDays(2), quantumId) } returns details
      every { sqlRepository.getDetailTemplates(listOf(templateName)) } returns templates

      val returnValue = service.getStaffDetails(from, to.plusDays(2), quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to.plusDays(2), quantumId) }
      verify { sqlRepository.getDetailTemplates(listOf(templateName)) }

      assertThat(returnValue).hasSize(6)
      assertThat(returnValue.elementAt(0).detailStart).isEqualTo(
        calculateDetailDateTime(
          day1,
          detailStart1 + templateStart
        )
      )
      assertThat(returnValue.elementAt(1).detailStart).isEqualTo(calculateDetailDateTime(day1, templateStart))
      assertThat(returnValue.elementAt(2).detailStart).isEqualTo(
        calculateDetailDateTime(
          day2,
          detailStart2 + templateStart
        )
      )
      assertThat(returnValue.elementAt(3).detailStart).isEqualTo(calculateDetailDateTime(day2, templateStart))
      assertThat(returnValue.elementAt(4).detailStart).isEqualTo(
        calculateDetailDateTime(
          day3,
          detailStart3 + templateStart
        )
      )
      assertThat(returnValue.elementAt(5).detailStart).isEqualTo(calculateDetailDateTime(day3, templateStart))
    }
  }

  @Nested
  @DisplayName("Get Details Detail Time tests")
  inner class ServiceTaskTimeTests {

    @Test
    fun `Should subtract time when start time less than 0`() {
      val details = listOf(getValidShiftDetail(-1234L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().minusSeconds(1234))
    }

    @Test
    fun `Should replace start full day magic number with 0`() {
      val details = listOf(getValidShiftDetail(-2147483648L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay())
    }

    @Test
    fun `Should replace end full day magic number with 0`() {
      val details = listOf(getValidShiftDetail(123L, -2147483648L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay())
    }

    @Test
    fun `Should replace start time of 86400 with time plus 0`() {
      val details = listOf(getValidShiftDetail(86400L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().plusSeconds(0))
    }

    @Test
    fun `Should replace end time of 86400 with time plus 0`() {
      val details = listOf(getValidShiftDetail(123L, 86400L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { authenticationFacade.currentUsername } returns quantumId

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().plusSeconds(0))
    }

    @Test
    fun `Should add start time of 86401 as 86401`() {
      val details = listOf(getValidShiftDetail(86401L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { authenticationFacade.currentUsername } returns quantumId

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().plusSeconds(86401))
    }

    @Test
    fun `Should add end time of 86401 as 86401`() {
      val details = listOf(getValidShiftDetail(123L, 86401L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { authenticationFacade.currentUsername } returns quantumId

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().plusSeconds(86401))
    }

    @Test
    fun `Should subtract less than 0 start time`() {
      val details = listOf(getValidShiftDetail(-123L, 456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details
      every { authenticationFacade.currentUsername } returns quantumId

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailStart).isEqualTo(shiftDate.atStartOfDay().minusSeconds(123))
    }

    @Test
    fun `Should subtract less than 0 end time`() {
      val details = listOf(getValidShiftDetail(123L, -456L))
      every { sqlRepository.getDetails(paddedFrom, to, quantumId) } returns details

      val returnValue = service.getStaffDetails(from, to, quantumId)

      verify { sqlRepository.getDetails(paddedFrom, to, quantumId) }

      assertThat(returnValue).hasSize(1)
      assertThat(returnValue.first().detailEnd).isEqualTo(shiftDate.atStartOfDay().minusSeconds(456))
    }
  }

  @Nested
  @DisplayName("Delete processed tests")
  inner class DeleteProcessedTests {
    @Test
    fun `Should split large id array into several SQL calls`() {
      val ids = List<Long>(1002) { it + 1L }
      val chunk1 = List<Long>(1000) { it + 1L }
      val chunk2 = List<Long>(2) { it + 1001L }
      every { sqlRepository.deleteProcessed(any()) } returns 1

      service.deleteProcessed(2, ids)

      verifySequence {
        sqlRepository.deleteProcessed(chunk1)
        sqlRepository.deleteProcessed(chunk2)
      }
      assertThat(RegionContext.getRegion()).isEqualTo("2")
    }

    @Test
    fun `Should do small id array in one go`() {
      val ids = List<Long>(10) { it + 1L }
      every { sqlRepository.deleteProcessed(any()) } returns 1

      service.deleteProcessed(2, ids)

      verifySequence {
        sqlRepository.deleteProcessed(ids)
      }
    }
  }

  private fun getValidShiftDetail(start: Long, end: Long): Detail {

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

  private fun getValidShiftDetailWithTemplateName(
    shiftDate: LocalDate,
    start: Long,
    end: Long,
    templateName: String
  ): Detail {

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
      templateName
    )
  }

  private fun getValidDetailTemplate(
    start: Long,
    end: Long,
    isRelative: Boolean,
    templateName: String
  ): DetailTemplate {
    val activity = "Phone Center"

    return DetailTemplate(
      start,
      end,
      isRelative,
      activity,
      templateName
    )
  }

  private fun calculateDetailDateTime(shiftDate: LocalDate, duration: Long): LocalDateTime {
    // plusSeconds allows negative numbers.
    return shiftDate.atStartOfDay().plusSeconds(duration)
  }
}
