plugins {
  id("org.springframework.boot") version "3.4.0"
  id("io.spring.dependency-management") version "1.1.6"
  java
}

java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  implementation("org.liquibase:liquibase-core")

  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")

  implementation ("org.projectlombok:lombok:1.18.34")
  annotationProcessor ("org.projectlombok:lombok:1.18.34")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> { useJUnitPlatform() }
