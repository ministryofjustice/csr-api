package uk.gov.justice.digital.hmpps.csr.api.model

data class DetailTemplate(
        var detailStart: Long,
        var detailEnd: Long,
        val isRelative: Boolean,
        val activity: String,
        val templateName: String
)