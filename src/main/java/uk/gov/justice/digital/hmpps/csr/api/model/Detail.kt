package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate

data class Detail(

        var date: LocalDate,

        var startTimeInSeconds: Long,

        var endTimeInSeconds: Long,

        var shiftType: Int,

        var activity: String
)
