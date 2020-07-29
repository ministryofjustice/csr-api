package uk.gov.justice.digital.hmpps.csr.api

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement
import uk.gov.justice.digital.hmpps.csr.api.config.RegionAwareRoutingSource
import uk.gov.justice.digital.hmpps.csr.api.utils.region.Regions
import javax.sql.DataSource

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@EnableConfigurationProperties
class CsrApiApplication {

    @Autowired
    lateinit var regionData: Regions

    @Bean
    fun dataSource(): DataSource {
        val dataSource: AbstractRoutingDataSource = RegionAwareRoutingSource()
        val targetDataSources: Map<Any, Any> = regionData.regions.map {
            it.name to
                    createDataSource(regionData.url,
                            it.username,
                            it.password,
                            it.schema,
                            regionData.dataname)
        }.toMap()
        dataSource.setTargetDataSources(targetDataSources)
        dataSource.afterPropertiesSet()
        return dataSource
    }

    fun createDataSource(url: String, username: String, password: String, currentSchema: String, dataSourceClassName: String): DataSource {
        val dataSource = HikariDataSource()
        dataSource.dataSourceClassName = dataSourceClassName
        dataSource.addDataSourceProperty("url", url)
        dataSource.addDataSourceProperty("user", username)
        dataSource.addDataSourceProperty("password", password)
        dataSource.addDataSourceProperty("currentSchema", currentSchema)
        return dataSource
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CsrApiApplication::class.java, *args)
        }
    }
}