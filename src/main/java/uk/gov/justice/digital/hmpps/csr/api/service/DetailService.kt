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
import kotlin.math.abs

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

    fun getModifiedDetailByPlanUnit(planUnit: String): Collection<DetailDto> {
        log.debug("Fetching modified shifts for $planUnit")
        val modifiedShifts = sqlRepository.getModifiedShifts(planUnit)
        log.info("Found ${modifiedShifts.size} modified shifts for $planUnit")

        log.debug("Fetching modified detail for $planUnit")
        val modifiedDetails = sqlRepository.getModifiedDetails(planUnit)
        log.info("Found ${modifiedDetails.size} modified details for $planUnit")

        return mapToDetailsDto(modifiedShifts + modifiedDetails)
    }

    companion object {
        private const val FULL_DAY_ACTIVITY = -2_147_483_648L
        private val log = LoggerFactory.getLogger(DetailService::class.java)

        private fun mapToDetailsDto(details: Collection<Detail>): Collection<DetailDto> {
            return details.map {
                val detailStart = calculateDetailDateTime(it.shiftDate, it.startTimeInSeconds ?: 0L)
                val detailEnd = calculateDetailDateTime(it.shiftDate, it.endTimeInSeconds ?: 0L)
                DetailDto.from(it, detailStart, detailEnd)
            }
        }

        private fun calculateDetailDateTime(shiftDate: LocalDate, detailTime: Long): LocalDateTime {
            val shiftDateTime = shiftDate.atStartOfDay()
            // If the start value is a negative number the shift detail actually starts the previous day
            return when {
                detailTime in (FULL_DAY_ACTIVITY + 1)..-1 -> {
                    shiftDateTime.minusSeconds(abs(detailTime))
                }
                detailTime > 0 -> {
                    shiftDateTime.plusSeconds(detailTime)
                }
                else -> {
                    shiftDateTime
                }
            }
        }
    }
}
