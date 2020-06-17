package uk.gov.justice.digital.hmpps.csr.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CsrApiApplication

fun main(args: Array<String>) {
	runApplication<CsrApiApplication>(*args)
}
