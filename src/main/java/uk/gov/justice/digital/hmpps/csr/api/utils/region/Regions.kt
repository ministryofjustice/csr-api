package uk.gov.justice.digital.hmpps.csr.api.utils.region

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext

@ConfigurationProperties(prefix = "csr")
@Configuration
class Regions {
  lateinit var regions: List<Region>
  init {
    // Startup set to R1 for Flyway's benefit
    RegionContext.setRegion(1)
  }
}

class Region {
  lateinit var name: String
  lateinit var username: String
  lateinit var password: String
  lateinit var url: String
  lateinit var driverClassName: String
  lateinit var schema: String
}
