package uk.gov.justice.digital.hmpps.csr.api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(value = ["test"])
class CsrApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
