package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity

data class ShiftOvertime(
        @Id
        @Column(nullable = false, name = "Date")
        var date: LocalDate,

        @Column(nullable = false, name = "DateTimeStamp")
        var dateTimeStamp: LocalDateTime,

        @Column(nullable = false, name = "StaffId")
        var staffId: Int,

        @Column(nullable = false, name = "TaskStartDateTimeInSeconds")
        var detailStartTimeInSeconds: Long,

        @Column(nullable = false, name = "TaskEndDateTimeInSeconds")
        var detailEndTimeInSeconds: Long,

        @Column(nullable = false, name = "Activity")
        var task: String
)
