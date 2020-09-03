package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Detail(

        val quantumId: String?,

        val shiftModified: LocalDateTime?,

        var shiftDate: LocalDate,

        val entityType: Int,

        var startTimeInSeconds: Long?,

        var endTimeInSeconds: Long?,

        val activity: String?,

        val detailType: Int?,

        val actionType: Int?
)
