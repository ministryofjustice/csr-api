package uk.gov.justice.digital.hmpps.csr.api.config

object ThreadLocalStorage {
    private val regionStore = ThreadLocal<String>()

    var region: String?
        get() = regionStore.get()
        set(region) {
            regionStore.set(region)
        }
}