package uk.gov.justice.digital.hmpps.csr.api.utils

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
@Order(4)
@Deprecated("Remove this filter once the legacy endpoints are no longer needed. New endpoints set region from the url, not the header")
class RegionContextFilter : Filter {
  @Throws(IOException::class, ServletException::class)
  override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
    val httpServletRequest = servletRequest as HttpServletRequest
    // Read in the region header and set a threadlocal value
    // for use in looking up the correct datasource
    httpServletRequest.getHeader("X-Region")?.also { RegionContext.setRegion(it.toInt()) }
    filterChain.doFilter(httpServletRequest, servletResponse)
  }

  override fun init(filterConfig: FilterConfig) {}
  override fun destroy() {}
}
