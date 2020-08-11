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
        log.info("Fetching notifications")
        return notificationRepository
            .GET_MODIFIED_SHIFTS(planUnit)
            .map { ShiftNotificationDto.from(it) }
    }

    companion object {

        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
