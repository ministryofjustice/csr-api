package uk.gov.justice.digital.hmpps.csr.api.domain

import java.util.*

enum class ShiftType(val shiftType: String, val number: Int) {
    SHIFT("shift", 0),
    OVERTIME("overtime", 1);

    companion object {
        private val map = values().associateBy(ShiftType::number)
        fun fromInt(type: Int) = map[type]
    }
}