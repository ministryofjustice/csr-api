package uk.gov.justice.digital.hmpps.csr.api.domain

enum class ActionType(val value: Int) {
  UNCHANGED(0),
  ADD(1),
  EDIT(2),
  DELETE(3);

  companion object {
    private val map = values().associateBy(ActionType::value)
    fun from(type: Int) = map.getOrDefault(type, EDIT)
  }
}
