import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("io.spring.dependency-management") version "1.1.3"
    id("org.springframework.boot") version "3.1.3"

    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.jetbrains.kotlinx.kover") version "0.7.3"

    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("io.github.logrecorder:logrecorder-bom:2.7.0")
        mavenBom("org.jetbrains.kotlin:kotlin-bom:1.8.22")
        mavenBom("org.zalando:logbook-bom:3.4.0")

        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.4")
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
    dependencies {
        dependency("com.ninja-squad:springmockk:4.0.2")
        dependency("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
        dependency("io.mockk:mockk-jvm:1.13.5")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.zalando:logbook-logstash")
    implementation("org.zalando:logbook-spring-boot-starter")

    testImplementation("org.springframework:spring-webflux") // for WebTestClient
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("com.ninja-squad:springmockk")
    testImplementation("io.github.logrecorder:logrecorder-assertions")
    testImplementation("io.github.logrecorder:logrecorder-junit5")
    testImplementation("io.github.logrecorder:logrecorder-logback")
    testImplementation("io.mockk:mockk-jvm")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xjsr305=strict"
            javaParameters = true
        }
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(SKIPPED, FAILED)
            showExceptions = true
            showStackTraces = true
            exceptionFormat = FULL
        }
    }
}

tasks {
    asciidoctor {
        inputs.dir(file("build/generated-snippets"))
        dependsOn(test)
        baseDirFollowsSourceDir()
        forkOptions {
            jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
        }
        options(
            mapOf(
                "doctype" to "book",
                "backend" to "html5"
            )
        )
        attributes(
            mapOf(
                "snippets" to file("build/generated-snippets"),
                "source-highlighter" to "coderay",
                "toclevels" to "3",
                "sectlinks" to "true",
                "data-uri" to "true",
                "nofooter" to "true"
            )
        )
    }
    bootJar {
        dependsOn(asciidoctor)
        from(asciidoctor) {
            into("BOOT-INF/classes/static/docs")
        }
    }
    build {
        dependsOn(koverHtmlReport)
    }
    test {
        outputs.dir(file("build/generated-snippets"))
    }
}

asciidoctorj {
    fatalWarnings("include file not found") // make build fail if generated files are missing
    modules {
        diagram.use()
        diagram.setVersion("2.2.13")
    }
}

detekt {
    allRules = true
    buildUponDefaultConfig = true
    config.from("configurations/detekt.yml")
}
