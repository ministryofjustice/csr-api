package uk.gov.justice.digital.hmpps.csr.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CmdNotification(
  var id: Long,
  val staffId: Int,
  val levelId: Int,
  var onDate: LocalDate,
  val layer: Int,
  val quantumId: String?,
  val lastModified: LocalDateTime,
  val actionType: Int?,
  val startTimeInSeconds: Long?,
  val endTimeInSeconds: Long?,
  val planunit: String?,
  val activity: String?,
)
