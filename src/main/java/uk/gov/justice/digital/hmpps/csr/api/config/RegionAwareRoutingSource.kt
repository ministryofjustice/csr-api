package uk.gov.justice.digital.hmpps.csr.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import uk.gov.justice.digital.hmpps.csr.api.config.ThreadLocalStorage.region
import java.io.File
import java.util.*
import java.util.stream.Collectors
import javax.sql.DataSource

class RegionAwareRoutingSource @JvmOverloads constructor(private val filename: String, private val objectMapper: ObjectMapper = ObjectMapper()) : AbstractRoutingDataSource() {
    private val regions: Map<String, HikariDataSource>
    override fun afterPropertiesSet() {}
    override fun determineTargetDataSource(): DataSource {
        val lookupKey = determineCurrentLookupKey() as String
        return regions[lookupKey]!!
    }

    override fun determineCurrentLookupKey(): Any {
        return region!!
    }

    private val dataConfigurations: Map<String, HikariDataSource>
        get() {
            val configurations: Array<DatabaseConfiguration>
            configurations = try {
                objectMapper.readValue(File(filename), Array<DatabaseConfiguration>::class.java)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            return Arrays
                    .stream(configurations)
                    .collect(Collectors.toMap({ obj: DatabaseConfiguration -> obj.region }) { configuration: DatabaseConfiguration -> buildDataSource(configuration) })
        }

    private fun buildDataSource(configuration: DatabaseConfiguration): HikariDataSource {
        val dataSource = HikariDataSource()
        dataSource.initializationFailTimeout = 0
        dataSource.maximumPoolSize = 5
        dataSource.dataSourceClassName = configuration.dataSourceClassName
        dataSource.addDataSourceProperty("url", configuration.url)
        dataSource.addDataSourceProperty("user", configuration.user)
        dataSource.addDataSourceProperty("password", configuration.password)
        return dataSource
    }

    init {
        regions = dataConfigurations
    }
}