package uk.gov.justice.digital.hmpps.csr.api.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.csr.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.csr.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import uk.gov.justice.digital.hmpps.csr.api.model.DetailTemplate
import uk.gov.justice.digital.hmpps.csr.api.repository.SqlRepository
import uk.gov.justice.digital.hmpps.csr.api.security.AuthenticationFacade
import java.time.LocalDate

private const val DELETECHUNKSIZE = 1000

@Service
class DetailService(
  private val sqlRepository: SqlRepository,
  private val authenticationFacade: AuthenticationFacade,
) {
  val objectMapper: ObjectMapper = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
    .registerModules(Jdk8Module(), JavaTimeModule(), KotlinModule.Builder().build())

  fun getStaffDetails(
    from: LocalDate,
    to: LocalDate,
  ): Collection<DetailDto> {
    val quantumId = authenticationFacade.currentUsername
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

  fun getModifiedShiftsByPlanUnit(planUnit: String): Collection<DetailDto> {
    log.info("Fetching modified shifts for $planUnit")
    val startTime = System.currentTimeMillis()

    val modifiedShifts = sqlRepository.getModifiedShifts(planUnit)

    log.info("Found ${modifiedShifts.size} modified shifts for $planUnit, time taken ${elapsed(startTime)}s")
    val json = objectMapper.writeValueAsString(modifiedShifts)
    log.info("Return body size is ${json.length}")
    return mapToDetailsDto(modifiedShifts)
  }

  fun getModifiedDetailsByPlanUnit(planUnit: String): Collection<DetailDto> {
    log.info("Fetching modified detail for $planUnit")
    val startTime = System.currentTimeMillis()

    val modifiedDetails = sqlRepository.getModifiedDetails(planUnit)

    log.info("Found ${modifiedDetails.size} modified details for $planUnit, time taken ${elapsed(startTime)}s")
    return mapToDetailsDto(modifiedDetails)
  }

  fun getModified(): List<DetailDto> {
    val startTime = System.currentTimeMillis()

    val modified = mapCmdNotificationToDetailsDto(sqlRepository.getModified())

    log.info("getModified: Found ${modified.size}, time taken ${elapsed(startTime)}s")
    return modified
  }

  // Intentionally not transactional: we want chunks to get deleted even if one fails
  fun deleteProcessed(ids: List<Long>): String {
    val startTime = System.currentTimeMillis()

    ids.chunked(DELETECHUNKSIZE).forEach {
      try {
        val deleted = sqlRepository.deleteProcessed(it)
        log.info("deleteProcessed: deleted $deleted rows")
      } catch (e: Exception) {
        log.error("Unexpected exception", e)
      }
    }

    return "Received ${ids.size} ids, time taken ${elapsed(startTime)}s".also {
      log.info("deleteProcessed: $it")
    }
  }

  @Transactional
  fun deleteAll(): String {
    val startTime = System.currentTimeMillis()

    val deleted = sqlRepository.deleteAll()

    return "Deleted $deleted rows, time taken ${elapsed(startTime)}s".also {
      log.info("deleteAll: $it")
    }
  }

  @Transactional
  fun deleteOld(date: LocalDate): String {
    val startTime = System.currentTimeMillis()

    val deleted = sqlRepository.deleteOld(date)

    return "Deleted $deleted rows up to $date, time taken ${elapsed(startTime)}s".also {
      log.info("deleteOld: $it")
    }
  }

  private fun elapsed(startTime: Long) = (System.currentTimeMillis() - startTime) / 1000.0

  private fun getTemplates(templateNames: Collection<String>): Collection<DetailTemplate> {
    log.debug("Fetching templates: $templateNames")
    val templates = if (templateNames.any()) {
      sqlRepository.getDetailTemplates(templateNames)
    } else {
      setOf()
    }
    log.debug("Found ${templates.size}: templates")
    return templates
  }

  private fun mapToDetailsDto(details: Collection<Detail>): Collection<DetailDto> = details.map(DetailDto::from)

  private fun mapCmdNotificationToDetailsDto(notifications: List<CmdNotification>): List<DetailDto> =
    notifications.map(DetailDto::from)

  private fun mergeTemplatesIntoDetails(
    details: Collection<Detail>,
    templates: Collection<DetailTemplate>
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
              el.templateName
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
