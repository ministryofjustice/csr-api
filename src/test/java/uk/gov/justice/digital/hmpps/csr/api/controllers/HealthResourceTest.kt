package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.client.RestTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@AutoConfigureRestTestClient
class HealthResourceTest(@Autowired val restTestClient: RestTestClient) {

  val jsonTester = BasicJsonTester(this::class.java)

  @Test
  fun `Ping test`() {
    restTestClient.get().uri(PING_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody().jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health test`() {
    restTestClient.get().uri(HEALTH_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.status").isEqualTo("UP")
      .jsonPath("$.components.ping.status").isEqualTo("UP")
      .jsonPath("$.components.diskSpace.status").isEqualTo("UP")
  }

  @Test
  fun `Info test`() {
    restTestClient.get().uri(INFO_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.build.name").isEqualTo("csr-api")
  }

  companion object {
    private const val PING_URL = "/health/ping"
    private const val HEALTH_URL = "/health"
    private const val INFO_URL = "/info"
  }
}
