package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.DetailTemplate
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DetailService(private val sqlRepository: SqlRepository, val authenticationFacade: AuthenticationFacade) {

    fun getStaffDetails(from: LocalDate, to: LocalDate, quantumId: String = authenticationFacade.currentUsername): Collection<DetailDto> {
        log.debug("Fetching shift details for $quantumId")
        // We must pad the 'from' so that we don't miss night shift ends that start the day before our from-to range.
        val details = sqlRepository.getDetails(from.minusDays(1), to, quantumId)
        log.debug("Found ${details.size} shift details for $quantumId")

        // Some details are created from a template and we don't get the complete time or activity data
        // so we need to get a list of templates in our results and fetch the template data separately.
        val templates = getTemplates(details.mapNotNull { it.templateName }.distinct())

        // We merge details that refer to templates with the data from the template.
        val mergedDetails = mergeTemplatesIntoDetails(details, templates)
        log.info("Returning ${mergedDetails.size} shift details for $quantumId")

        return mapToDetailsDto(mergedDetails)
    }

    fun getModifiedDetailsByPlanUnit(planUnit: String): Collection<DetailDto> {
        log.info("Fetching modified shifts for $planUnit")
        val modifiedShifts = sqlRepository.getModifiedShifts(planUnit)

        log.info("Found ${modifiedShifts.size} modified shifts for $planUnit")

        log.info("Fetching modified detail for $planUnit")
        val modifiedDetails = sqlRepository.getModifiedDetails(planUnit)
        log.info("Found ${modifiedDetails.size} modified details for $planUnit")

        return mapToDetailsDto(modifiedShifts + modifiedDetails)
    }

    private fun getTemplates(templateNames : Collection<String>) : Collection<DetailTemplate> {
        log.debug("Fetching templates: $templateNames")
        val templates = if(templateNames.any()) {
            sqlRepository.getDetailTemplates(templateNames)
        } else {
            setOf()
        }
        log.debug("Found ${templates.size}: templates")
        return templates
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

    private fun mergeTemplatesIntoDetails(details: Collection<Detail>, templates: Collection<DetailTemplate>): Collection<Detail> {
        val groupedTemplates = templates.groupBy { it.templateName }

        return details
                .fold(listOf()) { acc: Collection<Detail>, el: Detail ->
                    // If the detail doesn't refer to a template, add it and continue
                    if (el.templateName == null) {
                        acc.plus(el)
                    }
                    else {
                        /*
                         Details that refer to templates still have a start and end time
                         One detail might be replaced by multiple template rows
                         Some row's times are relative to the detail start time
                         and some are not (usually Breaks).
                         If the times are relative we need to change them to absolute values
                         before constructing each replacement detail
                         */
                        val newDetails = groupedTemplates[el.templateName]?.map {
                            var start = it.detailStart
                            var end = it.detailEnd
                            if (it.isRelative && el.startTimeInSeconds != null) {
                                start += el.startTimeInSeconds
                                end += el.startTimeInSeconds
                            }
                            Detail(
                                    el.quantumId,
                                    el.shiftModified,
                                    el.shiftDate,
                                    el.shiftType,
                                    start,
                                    end,
                                    it.activity,
                                    el.actionType,
                                    el.templateName
                            )
                        }

                        // Add the new details instead of the detail that references the template
                        if (newDetails != null) {
                            acc.plus(newDetails)
                        }
                        else {
                            log.warn("Detail template could not be merged")
                            acc.plus(el)
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
