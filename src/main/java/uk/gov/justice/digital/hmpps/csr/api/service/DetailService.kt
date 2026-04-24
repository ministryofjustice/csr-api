package uk.gov.justice.digital.hmpps.csr.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.DetailTemplate
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate

@Service
class DetailService(
  private val sqlRepository: SqlRepository,
  private val authenticationFacade: HmppsAuthenticationHolder,
) {
  @Transactional(readOnly = true)
  fun getStaffDetails(
    from: LocalDate,
    to: LocalDate,
    regionSchema: String,
  ): Collection<DetailDto> {
    sqlRepository.setSchema(regionSchema)
    val quantumId = authenticationFacade.username!!
    log.debug("Fetching shift details for $quantumId")
    // We must pad the 'from' so that we don't miss night shift ends that start the day before our from-to range.
    val details = sqlRepository.getDetails(from.minusDays(1), to, quantumId)
    log.debug("Found {} shift details for {}", details.size, quantumId)

    // Some details are created from a template and we don't get the complete time or activity data
    // so we need to get a list of templates in our results and fetch the template data separately.
    val templates = getTemplates(details.mapNotNull { it.templateName }.distinct())

    // We merge details that refer to templates with the data from the template.
    val mergedDetails = mergeTemplatesIntoDetails(details, templates)
    log.info("Returning {} shift details for {}", mergedDetails.size, quantumId)

    return mapToDetailsDto(mergedDetails)
  }

  @Transactional(readOnly = true)
  fun getModified(regionSchema: String): List<DetailDto> {
    sqlRepository.setSchema(regionSchema)

    val startTime = System.currentTimeMillis()

    val modified = mapCmdNotificationToDetailsDto(sqlRepository.getModified())

    log.debug("getModified: Found {}, time taken {}s", modified.size, elapsed(startTime))
    return modified
  }

  @Transactional
  fun deleteProcessed(ids: List<Long>, regionSchema: String) {
    sqlRepository.setSchema(regionSchema)

    val deleted = sqlRepository.deleteProcessed(ids)
    log.info("deleteProcessed: deleted {} rows", deleted)
  }

  @Transactional
  fun deleteAll(regionSchema: String): String {
    sqlRepository.setSchema(regionSchema)

    val startTime = System.currentTimeMillis()

    val deleted = sqlRepository.deleteAll()

    return "Deleted $deleted rows, time taken ${elapsed(startTime)}s".also {
      log.info("deleteAll: $it")
    }
  }

  @Transactional
  fun deleteOld(date: LocalDate, regionSchema: String): String {
    sqlRepository.setSchema(regionSchema)

    val startTime = System.currentTimeMillis()

    val deleted = sqlRepository.deleteOld(date)

    return "Deleted $deleted rows up to $date, time taken ${elapsed(startTime)}s".also {
      log.info("deleteOld: $it")
    }
  }

  private fun elapsed(startTime: Long) = (System.currentTimeMillis() - startTime) / 1000.0

  private fun getTemplates(templateNames: Collection<String>): Collection<DetailTemplate> {
    log.debug("Fetching templates: {}", templateNames)
    val templates = if (templateNames.any()) {
      sqlRepository.getDetailTemplates(templateNames)
    } else {
      setOf()
    }
    log.debug("Found {}: templates", templates.size)
    return templates
  }

  private fun mapToDetailsDto(details: Collection<Detail>): Collection<DetailDto> = details.map(DetailDto::from)

  private fun mapCmdNotificationToDetailsDto(notifications: List<CmdNotification>): List<DetailDto> = notifications.map(DetailDto::from)

  private fun mergeTemplatesIntoDetails(
    details: Collection<Detail>,
    templates: Collection<DetailTemplate>,
  ): Collection<Detail> {
    val groupedTemplates = templates.groupBy { it.templateName }

    return details
      .fold(listOf()) { acc: Collection<Detail>, el: Detail ->
        // If the detail doesn't refer to a template, add it and continue
        if (el.templateName == null) {
          acc.plus(el)
        } else {
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
              el.templateName,
            )
          }

          // Add the new details instead of the detail that references the template
          if (newDetails != null) {
            acc.plus(newDetails)
          } else {
            log.warn("Detail template could not be merged: ${el.templateName}")
            acc.plus(el)
          }
        }
      }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DetailService::class.java)
  }
}
