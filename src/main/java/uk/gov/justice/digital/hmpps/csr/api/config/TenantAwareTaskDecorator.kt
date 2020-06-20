package uk.gov.justice.digital.hmpps.csr.api.config

import org.springframework.core.task.TaskDecorator
import uk.gov.justice.digital.hmpps.csr.api.config.ThreadLocalStorage.region

class TenantAwareTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val tenantName = region
        return Runnable {
            try {
                region = tenantName
                runnable.run()
            } finally {
                region = null
            }
        }
    }
}