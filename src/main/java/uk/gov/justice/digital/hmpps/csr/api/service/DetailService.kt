package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.repository.DetailRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate

@Service
@Transactional
class DetailService(
        private val detailRepository: DetailRepository,
        private val authenticationFacade: AuthenticationFacade
) {

    fun getStaffDetails(from: LocalDate, to: LocalDate, quantumId: String = authenticationFacade.currentUsername): Collection<DetailDto> {
        log.debug("Fetching shift details for $quantumId")
        val details = detailRepository.getDetails(from, to, quantumId)
                .map {
                    putDetailOnCorrectDateTime(it)
                    it
                }
        log.info("Found ${details.size} shift details for $quantumId")
        return DetailDto.from(details)
    }

    fun getModifiedDetailByPlanUnit(planUnit: String): Collection<DetailDto> {
        log.debug("Fetching modified shifts for $planUnit")
        val modifiedShifts = detailRepository.getModifiedShifts(planUnit)
        log.info("Found ${modifiedShifts.size} modified shifts for $planUnit")

        log.debug("Fetching modified detail for $planUnit")
        val modifiedDetails = detailRepository.getModifiedDetails(planUnit)
                .map {
                    putDetailOnCorrectDateTime(it)
                    it
                }
        log.info("Found ${modifiedDetails.size} modified details for $planUnit")

        return DetailDto.from(modifiedShifts + modifiedDetails)
    }

    companion object {
        private const val FULL_DAY_ACTIVITY = -2_147_483_648L
        private const val DAY_IN_SECONDS = 86_400L
        private val log = LoggerFactory.getLogger(DetailService::class.java)

        private fun putDetailOnCorrectDateTime(detail: Detail) {
            if (detail.startTimeInSeconds != null && detail.startTimeInSeconds!! < 0 && detail.startTimeInSeconds!! > FULL_DAY_ACTIVITY) {
                detail.shiftDate = detail.shiftDate.minusDays(1)
            }
            detail.startTimeInSeconds = detail.startTimeInSeconds?.let { time -> ensureTimeWithinDayBounds(time) }
            detail.endTimeInSeconds = detail.endTimeInSeconds?.let { time -> ensureTimeWithinDayBounds(time) }
        }

        // We shouldn't send times outside of what is supported by LocalTime.ofSecondOfDay()
        private fun ensureTimeWithinDayBounds(time: Long): Long {
            return when {
                time == FULL_DAY_ACTIVITY -> {
                    0
                }
                time < 0 -> {
                    time + DAY_IN_SECONDS
                }
                time >= DAY_IN_SECONDS -> {
                    time - DAY_IN_SECONDS
                }
                else -> {
                    time
                }
            }
        }

    }
}
