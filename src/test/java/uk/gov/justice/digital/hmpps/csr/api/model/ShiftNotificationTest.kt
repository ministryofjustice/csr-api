package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import java.time.*

class ShiftNotificationTest {

    @Test
    fun `Should return a valid ShiftDetail`() {

        val date = LocalDateTime.now(clock)

        val quantumId = "ABC"
        val staffId = 3
        val shiftDate = date.toLocalDate()
        val shiftModified = date.minusDays(3)
        val shiftModifiedInSeconds = shiftModified.toEpochSecond(ZoneOffset.of("Z"))
        val shiftType = ShiftType.SHIFT.number
        val actionType = ActionType.EDIT.number

        val detail = ShiftNotification(
                quantumId,
                date,
                staffId,
                shiftDate,
                shiftModified,
                shiftModifiedInSeconds,
                shiftType,
                actionType
        )

        Assertions.assertThat(detail.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(detail.staffId).isEqualTo(staffId)
        Assertions.assertThat(detail.shiftDate).isEqualTo(shiftDate)
        Assertions.assertThat(detail.lastModified).isEqualTo(shiftModified)
        Assertions.assertThat(detail.shiftType).isEqualTo(shiftType)
        Assertions.assertThat(detail.actionType).isEqualTo(actionType)

    }
        companion object {

            val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        }
    }