package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ST_STAFF")
data class ShiftNotification(
        @Id
        @Column(nullable = false, name = "QuantumId")
        var quantumId: String,

        @Column(nullable = false, name = "DateTimeStamp")
        var createdAt: LocalDateTime,

        @Column(nullable = false, name = "StaffId")
        var staffId: Int,

        @Column(nullable = false, name = "ShiftDate")
        var shiftDate: LocalDate,

        @Column(nullable = false, name = "LastModifiedDateTime")
        var lastModified: LocalDateTime,

        @Column(nullable = false, name = "LastModifiedDateTimeInSeconds")
        var lastModifiedInSeconds: Int,

        @Column(nullable = false, name = "Type")
        var shiftType: Int,

        @Column(nullable = false, name = "ActionType")
        var actionType: Int
)
