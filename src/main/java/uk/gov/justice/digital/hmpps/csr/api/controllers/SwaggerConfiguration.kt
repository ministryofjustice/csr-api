package uk.gov.justice.digital.hmpps.csr.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class SwaggerConfiguration(@Autowired val applicationContext: ApplicationContext) {

  @Bean
  fun serializingObjectMapper(): ObjectMapper? {
    val objectMapper = ObjectMapper()
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    objectMapper.registerModule(JavaTimeModule())
    objectMapper.registerModule(KotlinModule.Builder().build())
    return objectMapper
  }

  @Bean
  fun api(): OpenAPI {

    val buildProperties = try {
      applicationContext.getBean("buildProperties") as BuildProperties
    } catch (be: BeansException) {
      val properties = Properties()
      properties["version"] = "?"
      BuildProperties(properties)
    }

    return OpenAPI()
      .components(
        Components().addSecuritySchemes(
          "bearer-jwt",
          SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
        )
      )
      .info(
        Info().title("HMPPS CSR-API Documentation")
          .description("CSR data API for CMD.")
          .version(buildProperties.version)
          .license(
            License()
              .name("Open Government Licence v3.0")
              .url("https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
          )
          .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
      )
      .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
  }
}
