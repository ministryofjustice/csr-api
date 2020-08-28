package uk.gov.justice.digital.hmpps.csr.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DetailTypeTest {

    @Nested
    @DisplayName("Convert from Int")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match 0 and Unspecific`() {
            assertThat(DetailType.from(0)).isEqualTo(DetailType.UNSPECIFIC)
        }

        @Test
        fun `It should match 1 and Break`() {
            assertThat(DetailType.from(1)).isEqualTo(DetailType.BREAK)
        }

        @Test
        fun `It should match 2 and Illness`() {
            assertThat(DetailType.from(2)).isEqualTo(DetailType.ILLNESS)
        }

        @Test
        fun `It should match 3 and Holiday`() {
            assertThat(DetailType.from(3)).isEqualTo(DetailType.HOLIDAY)
        }

        @Test
        fun `It should match 4 and Absence`() {
            assertThat(DetailType.from(4)).isEqualTo(DetailType.ABSENCE)
        }

        @Test
        fun `It should match 5 and Meeting`() {
            assertThat(DetailType.from(5)).isEqualTo(DetailType.MEETING)
        }

        @Test
        fun `It should match 5 and OnCall`() {
            assertThat(DetailType.from(6)).isEqualTo(DetailType.ONCALL)
        }

    }
}