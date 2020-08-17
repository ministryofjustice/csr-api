package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftDetail
import uk.gov.justice.digital.hmpps.csr.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.csr.api.repository.NotificationRepository


@Service
@Transactional
class NotificationService(
        val notificationRepository: NotificationRepository
) {

    fun getShiftNotificationsAndDetails(planUnit:String): Collection<ShiftNotificationDto>{
        val notificationsAsDto = ShiftNotificationDto.fromShift(getShiftNotifications(planUnit))
        val detailsAsDto = ShiftNotificationDto.fromDetail(getShiftDetails(planUnit))

        return notificationsAsDto.plus(detailsAsDto)
    }

    private fun getShiftNotifications(planUnit:String): Collection<ShiftNotification> {
        log.debug("Fetching modified shifts")

        val modifiedShifts =  notificationRepository.getModifiedShifts(planUnit)

        log.info("Found ${modifiedShifts.size} modified shifts")
         return modifiedShifts
    }

    private fun getShiftDetails(planUnit:String): Collection<ShiftDetail> {
        log.debug("Fetching modified shifts")

        val modifiedShiftDetails =  notificationRepository.getModifiedDetail(planUnit)

        log.info("Found ${modifiedShiftDetails.size} modified shifts")
         return modifiedShiftDetails
    }

    companion object {

        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
