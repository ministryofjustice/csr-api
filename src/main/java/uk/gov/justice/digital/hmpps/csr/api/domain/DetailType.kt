package uk.gov.justice.digital.hmpps.csr.api.domain

enum class DetailType(val value: Int) {
    UNSPECIFIC(0),
    BREAK(1),
    ILLNESS(2),
    HOLIDAY(3),
    ABSENCE(4),
    MEETING(5),
    ONCALL(6);

    companion object {
        private val map = values().associateBy(DetailType::value)
        fun from(type: Int) = map.getOrDefault(type, UNSPECIFIC)
    }
}