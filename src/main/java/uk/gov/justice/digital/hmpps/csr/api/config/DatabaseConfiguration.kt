package uk.gov.justice.digital.hmpps.csr.api.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class DatabaseConfiguration @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
                                                                                         @param:JsonProperty("region") val region: String,
                                                                                         @param:JsonProperty("url") val url: String,
                                                                                         @param:JsonProperty("user") val user: String,
                                                                                         @param:JsonProperty("dataSourceClassName") val dataSourceClassName: String,
                                                                                         @param:JsonProperty("password") val password: String) {
}