package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.DetailType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.*

class DetailDtoTest {

    @Test
    fun `Create Overtime Dto`() {
        val detail = getFullyPopulatedDetail()
        val start = LocalDate.now(clock).atTime(LocalTime.MIN)
        val end = LocalDate.now(clock).atTime(LocalTime.MAX)
        val detailDto = DetailDto.from(detail, start, end)

        assertThat(detailDto.quantumId).isEqualTo(quantumId)
        assertThat(detailDto.shiftModified).isEqualTo(shiftModified)
        assertThat(detailDto.shiftType).isEqualTo(entityType)
        assertThat(detailDto.detailStart).isEqualTo(start)
        assertThat(detailDto.detailEnd).isEqualTo(end)
        assertThat(detailDto.activity).isEqualTo(activity)
        assertThat(detailDto.detailType).isEqualTo(detailType)
        assertThat(detailDto.actionType).isEqualTo(actionType)
    }

    @Test
    fun `Create Overtime Dto even if values are null`() {
        val detail = Detail(
                null,
                null,
                LocalDate.now(),
                ShiftType.SHIFT.value,
                null,
                null,
                null,
                null,
                null
        )
        val start = LocalDate.now(clock).atTime(LocalTime.MIN)
        val end = LocalDate.now(clock).atTime(LocalTime.MAX)
        val detailDto = DetailDto.from(detail, start, end)

        assertThat(detailDto.quantumId).isNull()
        assertThat(detailDto.shiftModified).isNull()
        assertThat(detailDto.detailStart).isEqualTo(start)
        assertThat(detailDto.detailEnd).isEqualTo(end)
        assertThat(detailDto.activity).isNull()
        assertThat(detailDto.detailType).isNull()
        assertThat(detailDto.actionType).isNull()
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        const val quantumId = "XYZ"
        val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
        val shiftDate: LocalDate = LocalDate.now(clock)
        const val detailStartTimeInSeconds = 7200L
        const val detailEndTimeInSeconds = 84500L
        val entityType = ShiftType.OVERTIME
        val actionType = ActionType.EDIT
        val detailType = DetailType.UNSPECIFIC
        const val activity = "Phone Center"

        fun getFullyPopulatedDetail(): Detail {

            return Detail(
                    quantumId,
                    shiftModified,
                    shiftDate,
                    entityType.value,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    activity,
                    detailType.value,
                    actionType.value
            )
        }
    }
}