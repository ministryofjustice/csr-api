package uk.gov.justice.digital.hmpps.csr.api.domain

enum class EntityType(val value: Int) {
    SHIFT(0),
    OVERTIME(1);

    companion object {
        private val map = values().associateBy(EntityType::value)
        fun from(type: Int) = map.getOrDefault(type, SHIFT)
    }
}