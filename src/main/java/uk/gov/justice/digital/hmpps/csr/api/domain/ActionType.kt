package uk.gov.justice.digital.hmpps.csr.api.domain

enum class ActionType(val action: String, val number: Int) {
    UNCHANGED("unchanged", 0),
    ADD("add", 1),
    EDIT("edit", 2),
    DELETE("delete", 3);


    companion object {
        private val map = values().associateBy(ActionType::number)
        fun fromInt(type: Int) = map[type]
    }
}