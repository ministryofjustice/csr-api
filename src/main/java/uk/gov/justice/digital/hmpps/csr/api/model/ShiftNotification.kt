package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ShiftNotification(
        @Id
        @Column(nullable = false, name = "QuantumId")
        var quantumId: String,

        @Column(nullable = false, name = "DateTimeStamp")
        var date: LocalDateTime,

        @Column(nullable = false, name = "StaffId")
        var staffId: Int,

        @Column(nullable = false, name = "ShiftDate")
        var shiftDate: LocalDate,

        @Column(nullable = false, name = "LastModifiedDateTime")
        var lastModified: LocalDateTime,

        @Column(nullable = false, name = "LastModifiedDateTimeInSeconds")
        var lastModifiedInSeconds: Long,

        @Column(nullable = false, name = "Type")
        var shiftType: Int,


        @Column(nullable = false, name = "ActionType")
        var actionType: Int
)
