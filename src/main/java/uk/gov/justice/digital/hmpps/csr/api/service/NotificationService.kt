package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.csr.api.repository.NotificationRepository


@Service
@Transactional
class NotificationService(
        val notificationRepository: NotificationRepository
) {

    fun getNotifications(planUnit:String): Collection<ShiftNotificationDto> {
        log.debug("Fetching modified shifts")

        val modifiedShifts =  notificationRepository
                .getModifiedShifts(planUnit)
                .map { ShiftNotificationDto.from(it) }

        log.info("Found ${modifiedShifts.size} modified shifts")
         return modifiedShifts
    }

    companion object {

        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
