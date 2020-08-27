package uk.gov.justice.digital.hmpps.csr.api.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.csr.api.domain.ActionType
import uk.gov.justice.digital.hmpps.csr.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.csr.api.model.Detail
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DetailDtoTest {

    @Test
    fun `Create Overtime Dto`() {
        val detail = getFullyPopulatedDetail()
        val detailDto = DetailDto.from(detail)

        assertThat(detailDto.quantumId).isEqualTo(quantumId)
        assertThat(detailDto.shiftModified).isEqualTo(shiftModified)
        assertThat(detailDto.shiftDate).isEqualTo(shiftDate)
        assertThat(detailDto.shiftType).isEqualTo(shiftType)
        assertThat(detailDto.detailStart).isEqualTo(detailStartTimeInSeconds)
        assertThat(detailDto.detailEnd).isEqualTo(detailEndTimeInSeconds)
        assertThat(detailDto.activity).isEqualTo(activity)
        assertThat(detailDto.actionType).isEqualTo(actionType)
    }

    @Test
    fun `Create Overtime Dto even if values are null`() {
        val detail = Detail(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )
        val detailDto = DetailDto.from(detail)

        assertThat(detailDto.quantumId).isNull()
        assertThat(detailDto.shiftModified).isNull()
        assertThat(detailDto.shiftDate).isNull()
        assertThat(detailDto.shiftType).isNull()
        assertThat(detailDto.detailStart).isNull()
        assertThat(detailDto.detailEnd).isNull()
        assertThat(detailDto.activity).isNull()
        assertThat(detailDto.actionType).isNull()
    }

    @Test
    fun `Create DetailDto collection from collection even if values are null`() {
        val details = listOf(getFullyPopulatedDetail())
        val detailDtos = DetailDto.from(details)

        assertThat(detailDtos).hasSize(1)

        val first = detailDtos.first()
        assertThat(first.quantumId).isEqualTo(quantumId)
        assertThat(first.shiftModified).isEqualTo(shiftModified)
        assertThat(first.shiftDate).isEqualTo(shiftDate)
        assertThat(first.shiftType).isEqualTo(shiftType)
        assertThat(first.detailStart).isEqualTo(detailStartTimeInSeconds)
        assertThat(first.detailEnd).isEqualTo(detailEndTimeInSeconds)
        assertThat(first.activity).isEqualTo(activity)
        assertThat(first.actionType).isEqualTo(actionType)
    }

    @Test
    fun `Create DetailDto collection from collection`() {
        val details = listOf(Detail(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ))
        val detailDtos = DetailDto.from(details)

        assertThat(detailDtos).hasSize(1)

        val first = detailDtos.first()
        assertThat(first.quantumId).isNull()
        assertThat(first.shiftModified).isNull()
        assertThat(first.shiftDate).isNull()
        assertThat(first.shiftType).isNull()
        assertThat(first.detailStart).isNull()
        assertThat(first.detailEnd).isNull()
        assertThat(first.activity).isNull()
        assertThat(first.actionType).isNull()
    }

    companion object {

        private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        const val quantumId = "XYZ"
        val shiftModified: LocalDateTime = LocalDateTime.now(clock).minusDays(3)
        val shiftDate: LocalDate = LocalDate.now(clock)
        const val detailStartTimeInSeconds = 7200L
        const val detailEndTimeInSeconds = 84500L
        val shiftType = ShiftType.OVERTIME
        val actionType = ActionType.EDIT
        const val activity = "Phone Center"

        fun getFullyPopulatedDetail(): Detail {

            return Detail(
                    quantumId,
                    shiftModified,
                    shiftDate,
                    shiftType.value,
                    detailStartTimeInSeconds,
                    detailEndTimeInSeconds,
                    activity,
                    actionType.value
            )
        }
    }
}