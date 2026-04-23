package uk.gov.justice.digital.hmpps.csr.api

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.csr.api.utils.region.CsrConfiguration
import javax.sql.DataSource

@SpringBootApplication
class CsrApiApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(CsrApiApplication::class.java, *args)
    }
  }
}

@Configuration
@EnableConfigurationProperties(CsrConfiguration::class)
class CsrRegionDataSourceConfiguration(@Autowired private val regionData: CsrConfiguration) {
  @Bean
  fun regionDataSource(): DataSource = HikariDataSource().also {
    it.driverClassName = regionData.driverClassName
    it.jdbcUrl = regionData.url
    it.username = regionData.username
    it.password = regionData.password
    it.maximumPoolSize = 20
    // Startup set to R1 for Flyway's benefit
    it.schema = regionData.regions[0].schema
  }
}
