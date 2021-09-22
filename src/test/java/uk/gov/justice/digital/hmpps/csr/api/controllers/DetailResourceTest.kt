package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.json.BasicJsonTester
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

class DetailResourceTest : ResourceTest() {

  companion object {
    private val SYSTEM_ROLE = listOf("ROLE_SYSTEM_USER")
    private val SYSTEM_READ_ONLY_ROLE = listOf("ROLE_SYSTEM_READ_ONLY")
    private val TODAY_START = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
  }

  val jsonTester = BasicJsonTester(this::class.java)

  @Test
  fun testShifts() {
    val response = webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/shifts/updated")
          .build("Frankland")
      }
      .headers(setAuthorisation(roles = SYSTEM_ROLE))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(DetailDto::class.java)
      .returnResult()

    assertThat(response.responseBody).containsExactlyInAnyOrder(
      DetailDto(
        "a_1152",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.ADD
      ),
      DetailDto(
        "a_1152",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START.plusDays(1),
        detailEnd = TODAY_START.plusDays(1),
        activity = null,
        actionType = ActionType.ADD
      ),
      DetailDto(
        "a_1154",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
      DetailDto(
        "a_1155",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.EDIT
      ),
      DetailDto(
        "a_1156",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
      DetailDto(
        "a_1157",
        LocalDateTime.of(2099, Month.AUGUST, 21, 0, 0),
        shiftType = ShiftType.SHIFT,
        detailStart = TODAY_START,
        detailEnd = TODAY_START,
        activity = null,
        actionType = ActionType.DELETE
      ),
    )
  }

  // @Test
  fun testShifts_unauthorised() {
    webTestClient.get()
      .uri("/planUnit/somewhere/shifts/updated")
      .headers(setAuthorisation(roles = SYSTEM_READ_ONLY_ROLE))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun testDetails() {
    webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/details/updated")
          .build("Frankland")
      }
      .headers(setAuthorisation(roles = SYSTEM_ROLE))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(DetailDto::class.java)
      .hasSize(0)
  }

  // @Test
  fun testDetails_unauthorised() {
    webTestClient.get()
      .uri("/planUnit/somewhere/details/updated")
      .headers(setAuthorisation(roles = SYSTEM_READ_ONLY_ROLE))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
