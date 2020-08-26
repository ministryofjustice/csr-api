package uk.gov.justice.digital.hmpps.csr.api.domain

enum class ShiftType(val value: Int) {
    SHIFT(0),
    OVERTIME(1);

    companion object {
        private val map = values().associateBy(ShiftType::value)
        fun from(type: Int) = map.getOrDefault(type, SHIFT)
    }
}