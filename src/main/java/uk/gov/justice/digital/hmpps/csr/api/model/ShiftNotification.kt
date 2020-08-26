package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class ShiftNotification(

        var quantumId: String,

        var shiftDate: LocalDate,

        var lastModified: LocalDateTime,

        var shiftType: Int,

        var actionType: Int
)
