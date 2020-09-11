package uk.gov.justice.digital.hmpps.csr.api.model

data class TemplateDetail(
        var detailStart: Long,
        var detailEnd: Long,
        val isRelative: Boolean,
        val detail: String,
        val modelName: String
)