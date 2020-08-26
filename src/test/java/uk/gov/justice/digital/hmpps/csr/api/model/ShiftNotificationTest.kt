package uk.gov.justice.digital.hmpps.csr.api.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class ShiftNotificationTest {

    @Test
    fun `Should return a valid ShiftNotification`() {

        val date = LocalDate.now(clock)

        val quantumId = "ABC"
        val shiftModified = date.minusDays(3).atStartOfDay()
        val shiftType = ShiftType.SHIFT.value
        val actionType = ActionType.EDIT.value

        val detail = ShiftNotification(
                quantumId,
                date,
                shiftModified,
                shiftType,
                actionType
        )

        Assertions.assertThat(detail.quantumId).isEqualTo(quantumId)
        Assertions.assertThat(detail.shiftDate).isEqualTo(date)
        Assertions.assertThat(detail.lastModified).isEqualTo(shiftModified)
        Assertions.assertThat(detail.shiftType).isEqualTo(shiftType)
        Assertions.assertThat(detail.actionType).isEqualTo(actionType)

    }
        companion object {
            val clock: Clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        }
    }