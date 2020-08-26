package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DetailNotification(

        var quantumId: String,

        var shiftDate: LocalDate,

        var lastModified: LocalDateTime,

        var detailStartTimeInSeconds: Long,

        var detailEndTimeInSeconds: Long,

        var task: String,

        var shiftType: Int
)
