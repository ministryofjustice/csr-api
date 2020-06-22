package uk.gov.justice.digital.hmpps.csr.api.config.async

import org.springframework.core.task.TaskDecorator
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext.getRegion
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext.setRegion

class RegionAwareTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val regionName = getRegion()
        return Runnable {
            try {
                setRegion(regionName)
                runnable.run()
            } finally {
                setRegion(null)
            }
        }
    }
}