package uk.gov.justice.digital.hmpps.csr.api.utils

object RegionContext {
  private val regionStore = ThreadLocal<String>()
  fun getRegion(): String? {
    return regionStore.get()
  }

  fun setRegion(region: String?) {
    regionStore.set(region)
  }
}
