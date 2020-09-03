package uk.gov.justice.digital.hmpps.csr.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
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
        dataSource.addDataSourceProperty("user", region.username)
        dataSource.addDataSourceProperty("password", region.password)
        dataSource.addDataSourceProperty("currentSchema", region.schema)
        return dataSource
    }

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper? {
        return ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .registerModules(Jdk8Module(), JavaTimeModule(), KotlinModule())
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CsrApiApplication::class.java, *args)
        }
    }
}