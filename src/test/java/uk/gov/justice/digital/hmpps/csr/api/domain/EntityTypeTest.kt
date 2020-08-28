package uk.gov.justice.digital.hmpps.csr.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EntityTypeTest {

    @Nested
    @DisplayName("Convert from Int")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match 0 and Shift`() {
            assertThat(EntityType.from(0)).isEqualTo(EntityType.SHIFT)
        }

        @Test
        fun `It should match 1 and Overtime`() {
            assertThat(EntityType.from(1)).isEqualTo(EntityType.OVERTIME)
        }

    }
}