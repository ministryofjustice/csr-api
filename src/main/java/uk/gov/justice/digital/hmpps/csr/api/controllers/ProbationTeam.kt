package uk.gov.justice.digital.hmpps.csr.api.controllers

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class ProbationTeam(@Column(nullable = false) var functionalMailbox: String)