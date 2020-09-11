package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.TemplateDetail
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DetailService(private val sqlRepository: SqlRepository, val authenticationFacade: AuthenticationFacade) {

    fun getStaffDetails(from: LocalDate, to: LocalDate, quantumId: String = authenticationFacade.currentUsername): Collection<DetailDto> {
        log.debug("Fetching shift details for $quantumId")
        val details = sqlRepository.getDetails(from, to, quantumId)

        //TODO: add in template data
        log.info("Found ${details.size} shift details for $quantumId")

        return mapToDetailsDto(details)
    }

    fun getModifiedDetailsByPlanUnit(planUnit: String): Collection<DetailDto> {
        log.debug("Fetching modified shifts for $planUnit")
        val modifiedShifts = sqlRepository.getModifiedShifts(planUnit)

        //TODO: add in template data
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
        val normalisedTime = if (detailTime == 86400L) {
            0
        } else {
            detailTime
        }

        return if (normalisedTime != FULL_DAY_ACTIVITY) {
            // plusSeconds allows negative numbers.
            shiftDate.atStartOfDay().plusSeconds(normalisedTime)
        } else {
            shiftDate.atStartOfDay()
        }
    }

    private fun getModelNameSet(details: Collection<Detail>): Set<String> {
        return details
                .mapNotNull { it.modelName }
                .toSet()
    }

    private fun mergeTemplatesIntoDetails(details: Collection<Detail>, templates: Collection<TemplateDetail>): Collection<Detail> {
        val groupedTemplates = templates.groupBy { it.modelName }

        return details
                .fold<Detail, Collection<Detail>>(listOf()) { acc: Collection<Detail>, el: Detail ->
                    if (el.modelName == null) return acc.plus(el)
                    else {
                        val start = el.startTimeInSeconds
                        val end = el.endTimeInSeconds
                        val templates = groupedTemplates[el.modelName]?.map {
                            if (it.isRelative) {
                                if (start != null) {
                                    it.detailStart += start
                                }

                                if (end != null) {
                                    it.detailEnd += end
                                }
                            }
                            it
                        }

                        val newDetails = templates?.map {
                            Detail(
                                    el.quantumId,
                                    el.shiftModified,
                                    el.shiftDate,
                                    el.shiftType,
                                    it.detailStart,
                                    it.detailEnd,
                                    it.detail, //is this the correct value?
                                    el.actionType,
                                    el.modelName
                            )
                        }

                        return if (newDetails != null)
                            acc.plus(newDetails)
                        else {
                            log.warn("Detail template could not be merged")
                            acc
                        }
                    }
                }

    }

    companion object {
        // if both start and end are this magic number then detail is a full day activity
        private const val FULL_DAY_ACTIVITY = -2_147_483_648L

        private val log = LoggerFactory.getLogger(DetailService::class.java)
    }
}
