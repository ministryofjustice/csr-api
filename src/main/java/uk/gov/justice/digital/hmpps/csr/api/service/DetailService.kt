package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
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
                    if (it.startTimeInSeconds < 0) {
                        it.date = it.date.minusDays(1)
                    }
                    it.startTimeInSeconds = makeTimeRelative(it.startTimeInSeconds)
                    it.endTimeInSeconds = makeTimeRelative(it.endTimeInSeconds)
                    it
                }
        log.info("Found ${details.size} shift details for $quantumId")
        return DetailDto.from(details)
    }

    private fun makeTimeRelative(time: Long): Long {
        return when {
            time == FULL_DAY_ACTIVITY -> {
                0
            }
            time < 0 -> {
                time + DAY_IN_SECONDS
            }
            else -> {
                time
            }
        }
    }

    companion object {
        private const val FULL_DAY_ACTIVITY = -2147483648L
        private const val DAY_IN_SECONDS = 86400L
        private val log = LoggerFactory.getLogger(DetailService::class.java)
    }
}
