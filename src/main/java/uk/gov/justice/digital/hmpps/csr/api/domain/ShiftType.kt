package uk.gov.justice.digital.hmpps.csr.api.domain

import java.util.*

enum class ShiftType(val shiftType: String, val number: Int) {
    SHIFT("SHIFT", 0),
    OVERTIME("OVERTIME", 1);

    companion object {
        private val map = values().associateBy(ShiftType::number)
        fun fromInt(type: Int) = map[type]
    }
}