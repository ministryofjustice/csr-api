package uk.gov.justice.digital.hmpps.csr.api.repository

import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import java.util.*


@Repository
interface NotificationRepository : CrudRepository<ShiftNotification, UUID>{

    @Procedure("GET_MODIFIED_SHIFTS")
    fun GetModifiedShifts(planUnit:String): Collection<ShiftNotification>
}
