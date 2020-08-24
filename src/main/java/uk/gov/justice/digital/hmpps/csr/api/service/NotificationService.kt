package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.NotificationDto
import uk.gov.justice.digital.hmpps.csr.api.dto.NotificationsDto
import uk.gov.justice.digital.hmpps.csr.api.repository.NotificationRepository

@Service
@Transactional
class NotificationService(val notificationRepository: NotificationRepository) {

    fun getShiftAndDetailNotifications(planUnit: String): NotificationsDto {
        return NotificationsDto.from(
                getShiftNotifications(planUnit) +
                getDetailNotifications(planUnit)
        )
    }

    private fun getShiftNotifications(planUnit: String): Collection<NotificationDto> {
        log.debug("Fetching modified shifts for $planUnit")
        val modifiedShifts = notificationRepository.getModifiedShifts(planUnit)
        log.info("Found ${modifiedShifts.size} modified shifts for $planUnit")
        return NotificationDto.fromShift(modifiedShifts)
    }

    private fun getDetailNotifications(planUnit: String): Collection<NotificationDto> {
        log.debug("Fetching modified detail for $planUnit")
        val modifiedDetails = notificationRepository.getModifiedDetail(planUnit)
        log.info("Found ${modifiedDetails.size} modified details for $planUnit")
        return NotificationDto.fromDetail(modifiedDetails)
    }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
