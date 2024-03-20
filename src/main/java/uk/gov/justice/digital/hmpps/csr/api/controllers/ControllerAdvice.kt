package uk.gov.justice.digital.hmpps.csr.api.controllers

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps.csr.api.controllers"])
class ControllerAdvice {
  @ExceptionHandler(RestClientResponseException::class)
  fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<ByteArray> = ResponseEntity
    .status(e.statusCode)
    .body(e.responseBodyAsByteArray).also {
      log.error("Unexpected exception", e)
    }

  @ExceptionHandler(RestClientException::class)
  fun handleRestClientException(e: RestClientException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, developerMessage = e.message)).also {
      log.error("Unexpected exception", e)
    }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.FORBIDDEN)
    .body(ErrorResponse(status = HttpStatus.FORBIDDEN)).also {
      log.debug("Forbidden (403) returned: {}", e.message)
    }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(ErrorResponse(status = HttpStatus.BAD_REQUEST, developerMessage = e.message)).also {
      log.debug("Bad Request (400) returned: {}", e.message)
    }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleValidationException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> =
    ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(status = HttpStatus.BAD_REQUEST, developerMessage = e.message)).also {
        log.debug("Bad Request (400) returned: {}", e.message)
      }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.NOT_FOUND)
    .body(
      ErrorResponse(
        status = HttpStatus.NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, developerMessage = e.message)).also {
      log.error("Unexpected exception", e)
    }

  companion object {
    private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)
  }
}
