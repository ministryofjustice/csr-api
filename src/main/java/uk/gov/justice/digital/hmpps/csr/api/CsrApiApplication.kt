package uk.gov.justice.digital.hmpps.csr.api

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.scheduling.annotation.EnableAsync
import uk.gov.justice.digital.hmpps.csr.api.config.RegionAwareRoutingSource
import uk.gov.justice.digital.hmpps.csr.api.utils.region.Region
import uk.gov.justice.digital.hmpps.csr.api.utils.region.Regions
import javax.sql.DataSource

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
class CsrApiApplication {

  @Autowired
  lateinit var regionData: Regions

  @Bean
  fun dataSource(): DataSource {
    val dataSource: AbstractRoutingDataSource = RegionAwareRoutingSource()
    /*
    Read the region array in from application properties
    and construct datasources mapped to region names
    We then pass in the region as a header with the request
    and use this to select the right datasource
    */
    val targetDataSources = regionData.regions.map {
      it.name to regionDataSource(it)
    }

    dataSource.setTargetDataSources(targetDataSources.toMap())
    return dataSource
  }

  fun regionDataSource(region: Region): DataSource {
    val dataSource = HikariDataSource()
    dataSource.driverClassName = region.driverClassName
    dataSource.jdbcUrl = region.url
    dataSource.username = region.username
    dataSource.password = region.password
    return dataSource
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(CsrApiApplication::class.java, *args)
    }
  }
}
