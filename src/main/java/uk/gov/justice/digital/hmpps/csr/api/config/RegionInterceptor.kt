package uk.gov.justice.digital.hmpps.csr.api.config

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RegionInterceptor : HandlerInterceptorAdapter() {
    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        ThreadLocalStorage.region = request.getHeader("X-Region")
        return true
    }

    @Throws(Exception::class)
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception) {
        ThreadLocalStorage.region = null
    }
}