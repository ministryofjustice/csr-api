package uk.gov.justice.digital.hmpps.csr.api

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement
import uk.gov.justice.digital.hmpps.csr.api.config.RegionAwareRoutingSource
import java.util.*
import javax.sql.DataSource

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
class CsrApiApplication {

    @Bean
    fun dataSource(): DataSource {
        val dataSource: AbstractRoutingDataSource = RegionAwareRoutingSource()
        val targetDataSources: MutableMap<Any, Any> = HashMap()
        targetDataSources["TenantOne"] = tenantOne()
        targetDataSources["TenantTwo"] = tenantTwo()
        dataSource.setTargetDataSources(targetDataSources)
        dataSource.afterPropertiesSet()
        return dataSource
    }

    fun tenantOne(): DataSource {
        val dataSource = HikariDataSource()
        dataSource.initializationFailTimeout = 0
        dataSource.maximumPoolSize = 5
        dataSource.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        dataSource.addDataSourceProperty("url", "jdbc:postgresql://127.0.0.1:5432/csr-db")
        dataSource.addDataSourceProperty("user", "admin")
        dataSource.addDataSourceProperty("password", "admin_password")
        dataSource.addDataSourceProperty("currentSchema", "schema1")
        return dataSource
    }

    fun tenantTwo(): DataSource {
        val dataSource = HikariDataSource()
        dataSource.initializationFailTimeout = 0
        dataSource.maximumPoolSize = 5
        dataSource.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        dataSource.addDataSourceProperty("url", "jdbc:postgresql://127.0.0.1:5432/csr-db")
        dataSource.addDataSourceProperty("user", "admin")
        dataSource.addDataSourceProperty("password", "admin_password")
        dataSource.addDataSourceProperty("currentSchema", "schema2")
        return dataSource
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CsrApiApplication::class.java, *args)
        }
    }
}