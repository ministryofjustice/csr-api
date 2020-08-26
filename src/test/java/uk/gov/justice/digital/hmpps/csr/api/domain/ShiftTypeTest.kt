package uk.gov.justice.digital.hmpps.csr.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ShiftTypeTest {

    @Nested
    @DisplayName("Convert from Int")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match 0 and Shift`() {
            assertThat(ShiftType.from(0)).isEqualTo(ShiftType.SHIFT)
        }

        @Test
        fun `It should match 1 and Overtime`() {
            assertThat(ShiftType.from(1)).isEqualTo(ShiftType.OVERTIME)
        }

    }
}