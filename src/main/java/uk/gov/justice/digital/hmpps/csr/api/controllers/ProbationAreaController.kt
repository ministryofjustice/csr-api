package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
        value = ["csr"],
        produces = [APPLICATION_JSON_VALUE])
class ProbationAreaController(val localDeliveryUnitRepository: LocalDeliveryUnitRepository) {

    @GetMapping(path = ["/test"])

    fun getProbationArea(): String {
        val results = localDeliveryUnitRepository.findAllByIdEquals(0L)
        return results.get().get(0 ).firstName
    }

}

