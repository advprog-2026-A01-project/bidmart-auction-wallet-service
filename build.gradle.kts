import org.gradle.api.plugins.quality.Pmd

val springBootVersion: String by project
val springDependencyManagementVersion: String by project
val jjwtVersion: String by project
val grpcVersion: String by project
val protobufVersion: String by project
val grpcSpringBootStarterVersion: String by project
val protobufPluginVersion: String by project
val jakartaAnnotationVersion: String by project
val javaxAnnotationVersion: String by project
val sonarqubePluginVersion: String by project
val jacocoVersion: String by project
val pmdVersion: String by project

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
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    runtimeOnly("org.postgresql:postgresql")

    implementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootStarterVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
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
    toolVersion = pmdVersion
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
    toolVersion = jacocoVersion
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
        property(
            "sonar.coverage.exclusions",
            "**/id/ac/ui/cs/advprog/auctionwallet/grpc/**," +
            "**/AuctionWalletApplication.java"
        )
    }
}