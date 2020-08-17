package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ShiftDetail(
        @Id
        @Column(nullable = false, name = "QuantumId")
        var quantumId: String,

        @Column(nullable = false, name = "StaffId")
        var staffId: Int,

        @Column(nullable = false, name = "TaskDate")
        var shiftDate: LocalDate,

        @Column(nullable = false, name = "LastModifiedDateTime")
        var lastModified: LocalDateTime,

        @Column(nullable = false, name = "LastModifiedDateTimeInSeconds")
        var detailModifiedInSeconds: Long,

        @Column(nullable = false, name = "TaskStartTimeInSeconds")
        var detailStartTimeInSeconds: Long,

        @Column(nullable = false, name = "TaskEndTimeInSeconds")
        var detailEndTimeInSeconds: Long,

        @Column(nullable = false, name = "Activity")
        var task: String,

        @Column(nullable = false, name = "Type")
        var shiftType: Int
)
