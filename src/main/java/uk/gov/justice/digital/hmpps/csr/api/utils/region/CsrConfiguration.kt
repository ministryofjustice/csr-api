package uk.gov.justice.digital.hmpps.csr.api.utils.region

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "csr")
data class CsrConfiguration(
  val username: String,
  val password: String,
  val url: String,
  val driverClassName: String,
  val regions: List<Region>,
)

data class Region(
  val name: Int,
  val schema: String,
)
