package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class ShiftNotification(

        var quantumId: String,

        var date: LocalDateTime,

        var staffId: Int,

        var shiftDate: LocalDate,

        var lastModified: LocalDateTime,

        var lastModifiedInSeconds: Long,

        var shiftType: Int,

        var actionType: Int
)
