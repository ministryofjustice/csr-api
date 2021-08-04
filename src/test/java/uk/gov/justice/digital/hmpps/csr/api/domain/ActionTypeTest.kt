package uk.gov.justice.digital.hmpps.csr.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ActionTypeTest {

  @Nested
  @DisplayName("Convert from Int")
  inner class CaseInsensitiveFrom {
    @Test
    fun `It should match 0 and Unchanged`() {
      assertThat(ActionType.from(0)).isEqualTo(ActionType.UNCHANGED)
    }

    @Test
    fun `It should match 1 and Add`() {
      assertThat(ActionType.from(1)).isEqualTo(ActionType.ADD)
    }

    @Test
    fun `It should match 2 and Edit`() {
      assertThat(ActionType.from(2)).isEqualTo(ActionType.EDIT)
    }

    @Test
    fun `It should match 3 and Delete`() {
      assertThat(ActionType.from(3)).isEqualTo(ActionType.DELETE)
    }
  }
}
