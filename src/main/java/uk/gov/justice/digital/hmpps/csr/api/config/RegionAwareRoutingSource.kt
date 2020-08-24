package uk.gov.justice.digital.hmpps.csr.api.config

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import uk.gov.justice.digital.hmpps.csr.api.utils.RegionContext.getRegion

class RegionAwareRoutingSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        return getRegion() ?: "1"
    }
}