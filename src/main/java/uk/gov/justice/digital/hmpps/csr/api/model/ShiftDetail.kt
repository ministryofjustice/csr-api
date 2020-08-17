package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
data class ShiftDetail(
        @Id
        @Column(nullable = false, name = "QuantumId")
        var quantumId: String,

        @Column(nullable = false, name = "DateTimeStamp")
        var detailDate: LocalDateTime,

        @Column(nullable = false, name = "StaffId")
        var staffId: Int,

        @Column(nullable = false, name = "TaskDate")
        var taskDate: LocalDate,

        @Column(nullable = false, name = "LastModifiedDateTime")
        var detailModified: LocalDateTime,

        @Column(nullable = false, name = "LastModifiedDateTimeInSeconds")
        var detailModifiedInSeconds: Int,

        @Column(nullable = false, name = "TaskStartTimeInSeconds")
        var taskStartTimeInSeconds: Int,

        @Column(nullable = false, name = "TaskEndTimeInSeconds")
        var taskEndTimeInSeconds: Int,

        @Column(nullable = false, name = "Activity")
        var activity: String,

        @Column(nullable = false, name = "Type")
        var shiftDetailType: Int
)
