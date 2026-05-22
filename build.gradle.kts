import org.gradle.api.plugins.quality.Pmd

plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("pmd")
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
    id("com.google.protobuf") version "0.9.4"
}

group = "id.ac.ui.cs.advprog"
version = "0.0.1-SNAPSHOT"
description = "auction-wallet"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    runtimeOnly("org.postgresql:postgresql")

    implementation("net.devh:grpc-spring-boot-starter:3.1.0.RELEASE")
    implementation("io.grpc:grpc-stub:1.63.0")
    implementation("io.grpc:grpc-protobuf:1.63.0")
    implementation("com.google.protobuf:protobuf-java:3.25.3")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.grpc:grpc-testing:1.63.0")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

pmd {
    ruleSets = emptyList()
    isConsoleOutput = true
    toolVersion = "7.11.0"
}

tasks.withType<Pmd>().configureEach {
    ignoreFailures = false
    exclude("**/generated/**")
    exclude("**/build/generated/**")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Pmd>("pmdMain") {
    source = fileTree("src/main/java")
    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
}

tasks.named<Pmd>("pmdTest") {
    source = fileTree("src/test/java")
    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/java/main")) {

            exclude(
                "**/id/ac/ui/cs/advprog/auctionwallet/grpc/**"
            )
        }
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "bidmart-auction-wallet-service")
        property("sonar.organization", "advprog-2026-a01-project-1")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.exclusions", "**/grpc/**Proto*.java,**/grpc/**Grpc.java,build/generated/**")
        property("sonar.coverage.exclusions", "**/id/ac/ui/cs/advprog/auctionwallet/grpc/**")
    }
}