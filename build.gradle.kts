plugins {
    id("application")
    id("checkstyle")
    id("jacoco")
    id("io.freefair.lombok") version "8.6"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.sentry.jvm.gradle") version "5.5.0"
    id("org.sonarqube") version "6.0.1.5171"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "hexlet.code.AppApplication"
}

sentry {
    includeSourceContext = true

    org = "andrey-eryomenko"
    projectName = "task-manager"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

sonar {
    properties {
        property("sonar.projectKey", "ean3ena_java-project-99")
        property("sonar.organization", "ean3ena")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    runtimeOnly("com.h2database:h2")
    implementation("org.postgresql:postgresql:42.7.5")

    implementation("org.instancio:instancio-junit:3.6.0")
    implementation("net.datafaker:datafaker:2.0.1")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.sentryBundleSourcesJava {
    enabled = System.getenv("SENTRY_AUTH_TOKEN") != null
}
