package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class DetailService(
        private val sqlRepository: SqlRepository,
        private val authenticationFacade: AuthenticationFacade
) {

    fun getStaffDetails(from: LocalDate, to: LocalDate, quantumId: String = authenticationFacade.currentUsername): Collection<DetailDto> {
        log.debug("Fetching shift details for $quantumId")
        val details = sqlRepository.getDetails(from, to, quantumId)
        log.info("Found ${details.size} shift details for $quantumId")

        return mapToDetailsDto(details)
    }

    fun getModifiedDetailsByPlanUnit(planUnit: String): Collection<DetailDto> {
        log.debug("Fetching modified shifts for $planUnit")
        val modifiedShifts = sqlRepository.getModifiedShifts(planUnit)
        log.info("Found ${modifiedShifts.size} modified shifts for $planUnit")

        log.debug("Fetching modified detail for $planUnit")
        val modifiedDetails = sqlRepository.getModifiedDetails(planUnit)
        log.info("Found ${modifiedDetails.size} modified details for $planUnit")

        return mapToDetailsDto(modifiedShifts + modifiedDetails)
    }

    private fun mapToDetailsDto(details: Collection<Detail>): Collection<DetailDto> {
        return details.map {
            // We don't care about the shiftDate on its own
            // We want to include it in the detail's start/end values
            // So that our clients don't have to work it out themselves
            val detailStart = calculateDetailDateTime(it.shiftDate, it.startTimeInSeconds ?: 0L)
            val detailEnd = calculateDetailDateTime(it.shiftDate, it.endTimeInSeconds ?: 0L)
            DetailDto.from(it, detailStart, detailEnd)
        }
    }

    /*
        CSR database uses positive or negative numbers to offset the shiftDate.
        e.g. 04/09/2020T00:00:00 with a detail start of -10 is actually 03/09/2020T23:59:50
     */
    private fun calculateDetailDateTime(shiftDate: LocalDate, detailTime: Long): LocalDateTime {
        return if (detailTime != FULL_DAY_ACTIVITY) {
            // plusSeconds allows negative numbers.
            shiftDate.atStartOfDay().plusSeconds(detailTime)
        } else {
            shiftDate.atStartOfDay()
        }
    }

    companion object {
        // if both start and end are this magic number then detail is a full day activity
        private const val FULL_DAY_ACTIVITY = -2_147_483_648L

        private val log = LoggerFactory.getLogger(DetailService::class.java)
    }
}
