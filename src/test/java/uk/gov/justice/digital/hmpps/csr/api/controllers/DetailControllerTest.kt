package uk.gov.justice.digital.hmpps.csr.api.controllers

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.csr.api.service.DetailService
import uk.gov.justice.digital.hmpps.csr.api.utils.region.CsrConfiguration
import uk.gov.justice.digital.hmpps.csr.api.utils.region.Region

private const val REGION = "A_REGION"

@ExtendWith(MockKExtension::class)
@DisplayName("Detail Controller tests")
internal class DetailControllerTest {
  private val detailService: DetailService = mockk(relaxUnitFun = true)
  private val controller = DetailController(
    detailService,
    CsrConfiguration(
      username = "user",
      password = "pass",
      url = "url",
      driverClassName = "driver",
      regions = listOf(Region(1, REGION)),
    ),
  )

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(detailService)
  }

  @Nested
  @DisplayName("Delete processed tests")
  inner class DeleteProcessedTests {
    @Test
    fun `Should split large id array into several SQL calls`() {
      val ids = List(102) { it + 1L }
      val chunk1 = List(100) { it + 1L }
      val chunk2 = List(2) { it + 101L }
      every { detailService.deleteProcessed(any(), any()) } returns Unit

      controller.deleteProcessed(1, ids)

      verifySequence {
        detailService.deleteProcessed(chunk1, REGION)
        detailService.deleteProcessed(chunk2, REGION)
      }
    }

    @Test
    fun `Should do small id array in one go`() {
      val ids = List(10) { it + 1L }
      every { detailService.deleteProcessed(any(), any()) } returns Unit

      controller.deleteProcessed(1, ids)

      verifySequence {
        detailService.deleteProcessed(ids, REGION)
      }
    }

    @Test
    fun `Should continue after failure`() {
      val ids = List(102) { it + 1L }
      val chunk1 = List(100) { it + 1L }
      val chunk2 = List(2) { it + 101L }
      every { detailService.deleteProcessed(chunk1, REGION) } throws Exception("test")
      every { detailService.deleteProcessed(chunk2, REGION) } returns Unit

      controller.deleteProcessed(1, ids)

      verifySequence {
        detailService.deleteProcessed(chunk1, REGION)
        detailService.deleteProcessed(chunk2, REGION)
      }
    }
  }
}
