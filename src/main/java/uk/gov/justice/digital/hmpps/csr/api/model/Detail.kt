package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate

data class Detail(

        var date: LocalDate,

        var detailStartTimeInSeconds: Long,

        var detailEndTimeInSeconds: Long,

        var activity: String
)
