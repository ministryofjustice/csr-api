package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
class HealthResourceTest(@Autowired val testRestTemplate: TestRestTemplate) {

  val jsonTester = BasicJsonTester(this::class.java)

  @Test
  fun `Ping test`() {
    val response = testRestTemplate.getForEntity(PING_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.status", "UP")
  }

  @Test
  fun `Health test`() {
    val response = testRestTemplate.getForEntity(HEALTH_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.status", "UP")
    assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.components.ping.status", "UP")
    // TODO: db status - we don't have a DB yet.
    // assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.components.db.status", "UP")
    assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.components.diskSpace.status", "UP")
  }

  @Test
  fun `Info test`() {
    val response = testRestTemplate.getForEntity(INFO_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).hasJsonPathStringValue("$.build.name", "csr-api")
  }

  companion object {
    private const val PING_URL = "/health/ping"
    private const val HEALTH_URL = "/health"
    private const val INFO_URL = "/info"
  }
}
