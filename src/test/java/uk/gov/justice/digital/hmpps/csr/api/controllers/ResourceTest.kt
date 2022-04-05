package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.json.JsonContent
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.csr.api.utils.JwtAuthHelper
import java.time.Duration
import java.util.Objects
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class ResourceTest {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper
  fun createHttpEntityWithBearerAuthorisation(user: String?, roles: List<String>?): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, null)
  }

  private fun createHttpEntity(bearerToken: String, body: Any?): HttpEntity<*> {
    val headers = HttpHeaders()
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    if (body != null) {
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }
    return HttpEntity(body, headers)
  }

  fun assertThatStatus(response: ResponseEntity<String?>, status: Int) {
    assertThat(response.statusCodeValue).withFailMessage(
      "Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s",
      response.statusCodeValue,
      status,
      response.body
    ).isEqualTo(status)
  }

  fun assertThatJsonFileAndStatus(response: ResponseEntity<String?>, status: Int, jsonFile: String) {
    assertThatStatus(response, status)
    assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson(jsonFile)
  }

  private fun <T> getBodyAsJsonContent(response: ResponseEntity<String?>): JsonContent<T> {
    return JsonContent(javaClass, ResolvableType.forType(String::class.java), Objects.requireNonNull(response.body))
  }

  private fun createJwt(user: String?, roles: List<String>?): String {
    return jwtAuthHelper.createJwt(
      user,
      listOf("read", "write"),
      roles,
      Duration.ofHours(1),
      UUID.randomUUID().toString()
    )
  }

  internal fun setAuthorisation(
    user: String = "TEST-USER",
    roles: List<String> = listOf()
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
