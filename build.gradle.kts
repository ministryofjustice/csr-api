plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.5.0-beta"
  kotlin("plugin.spring") version "1.7.10"
  kotlin("plugin.jpa") version "1.7.10"
  idea
}

configurations {
  implementation { exclude(mapOf("module" to "tomcat-jdbc")) }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("com.zaxxer:HikariCP")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.16.0.0")

  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.11")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.11")
  implementation("org.springdoc:springdoc-openapi-security:1.6.11")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("com.nimbusds:nimbus-jose-jwt:9.24.3")
  implementation("com.google.guava:guava:31.1-jre")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.35.0")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("com.ninja-squad:springmockk:3.1.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}

// Language versions
java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "18"
  }
}
