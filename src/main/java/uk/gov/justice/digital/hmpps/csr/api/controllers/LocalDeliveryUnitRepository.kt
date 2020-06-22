package uk.gov.justice.digital.hmpps.csr.api.controllers

import org.springframework.data.repository.CrudRepository
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

interface LocalDeliveryUnitRepository : CrudRepository<Customer, Long> {

    @Async
    fun findAllByIdEquals(id : Long): CompletableFuture<List<Customer>>
}