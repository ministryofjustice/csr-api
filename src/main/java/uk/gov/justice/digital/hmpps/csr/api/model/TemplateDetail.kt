package uk.gov.justice.digital.hmpps.csr.api.model

data class TemplateDetail(
        val detailStart: Long,
        val detailEnd: Long,
        val isRelative: Boolean,
        val detail: String,
        val modelName: String
)