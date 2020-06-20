package uk.gov.justice.digital.hmpps.csr.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement
import uk.gov.justice.digital.hmpps.csr.api.config.RegionAwareRoutingSource
import javax.sql.DataSource

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
class CsrApiApplication

fun main(args: Array<String>) {
	runApplication<CsrApiApplication>(*args)
}

@Bean
fun dataSource(): DataSource? {
	return RegionAwareRoutingSource("regions.json")
}
