package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

data class DetailNotification(

        var quantumId: String,

        var staffId: Int,

        var shiftDate: LocalDate,

        var lastModified: LocalDateTime,

        var detailModifiedInSeconds: Long,

        var detailStartTimeInSeconds: Long,

        var detailEndTimeInSeconds: Long,

        var task: String,

        var shiftType: Int
)
