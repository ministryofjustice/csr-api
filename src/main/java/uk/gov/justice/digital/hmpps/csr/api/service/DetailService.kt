package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailsDto
import uk.gov.justice.digital.hmpps.csr.api.repository.DetailRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate

@Service
@Transactional
class DetailService(
        val detailRepository: DetailRepository,
        val authenticationFacade: AuthenticationFacade
) {

    fun getStaffDetails(from: LocalDate, to: LocalDate, quantumId : String = authenticationFacade.currentUsername):  DetailsDto {
        log.debug("Fetching shift details for $quantumId")
        val details = detailRepository.getDetails(from, to, quantumId)
        log.info("Found ${details.size} shift details for $quantumId")
        return DetailsDto.from(details)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DetailService::class.java)
    }
}
