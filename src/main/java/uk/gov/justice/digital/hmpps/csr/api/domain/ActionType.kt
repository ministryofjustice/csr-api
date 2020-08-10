package uk.gov.justice.digital.hmpps.csr.api.domain

enum class ActionType(val action: String, val number: Int) {
    UNCHANGED("UNCHANGED", 0),
    ADD("ADD", 1),
    EDIT("EDIT", 2),
    DELETE("DELETE", 3);


    companion object {
        private val map = values().associateBy(ActionType::number)
        fun fromInt(type: Int) = map[type]
    }
}