package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.DetailNotification
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification


@Repository
class NotificationRepository(private val jdbcTemplate: JdbcTemplate) {

    fun getModifiedShifts(planUnit: String): Collection<ShiftNotification> {
        return listOf()
    }

    fun getModifiedDetail(planUnit: String): Collection<DetailNotification> {
        return listOf()
    }
}
