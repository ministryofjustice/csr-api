package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto

class DetailResourceTest : ResourceTest() {

  companion object {
    private val SYSTEM_ROLE = listOf("ROLE_SYSTEM_USER")
    private val SYSTEM_READ_ONLY_ROLE = listOf("ROLE_SYSTEM_READ_ONLY")
  }

  @Test
  fun testOldDetails() {
    webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/details/modified")
          .build("Frankland")
      }
      .headers(setAuthorisation(roles = SYSTEM_ROLE))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(DetailDto.javaClass)
      .hasSize(0)
  }

  // @Test
  fun testOldDetails_unauthorised() {
    webTestClient.get()
      .uri("/planUnit/somewhere/details/modified")
      .headers(setAuthorisation(roles = SYSTEM_READ_ONLY_ROLE))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun testShifts() {
    webTestClient.get()
      .uri {
        it.path("/planUnit/{planUnit}/shifts/updated")
          .build("Frankland")
      }
      .headers(setAuthorisation(roles = SYSTEM_ROLE))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(DetailDto.javaClass)
      .hasSize(0)
      // TODO need to populate h2 db tables to get results
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
      .expectBodyList(DetailDto.javaClass)
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
