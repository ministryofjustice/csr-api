package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
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

    fun getDetail(from: LocalDate, to: LocalDate, quantumId : String = authenticationFacade.currentUsername): DetailsDto {
        return DetailsDto.from(
                getOvertimeDetails(from,to,quantumId) +
                getShiftDetails(from, to, quantumId)
        )
    }

    fun getOvertimeDetails(from: LocalDate, to: LocalDate, quantumId : String): Collection<DetailDto> {
        log.debug("Fetching overtime shift details for $quantumId")
        val overtimeDetails = detailRepository.getOvertimeDetails(from, to, quantumId)
        log.info("Found ${overtimeDetails.size} overtime shift details for $quantumId")
        return DetailDto.from(overtimeDetails)
    }

    fun getShiftDetails(from: LocalDate, to: LocalDate, quantumId : String): Collection<DetailDto> {
        log.debug("Fetching shift details for $quantumId")
        val shiftDetails = detailRepository.getShiftDetails(from, to, quantumId)
        log.info("Found ${shiftDetails.size} shift details for $quantumId")
        return DetailDto.from(shiftDetails)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DetailService::class.java)
    }
}
