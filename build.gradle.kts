import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.5"

    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"

    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
}

extra["snippetsDir"] = file("build/generated-snippets")

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("io.github.logrecorder:logrecorder-bom:2.10.0")
        mavenBom("org.jetbrains.kotlin:kotlin-bom:2.2.21")
        mavenBom("org.zalando:logbook-bom:4.0.3")

        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
    dependencies {
        dependency("com.ninja-squad:springmockk:5.0.1")
        dependency("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
        dependency("io.mockk:mockk-jvm:1.14.9")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.zalando:logbook-logstash")
    implementation("org.zalando:logbook-spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restdocs")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    testImplementation("com.ninja-squad:springmockk")
    testImplementation("io.github.logrecorder:logrecorder-assertions")
    testImplementation("io.github.logrecorder:logrecorder-junit5")
    testImplementation("io.github.logrecorder:logrecorder-logback")
    testImplementation("io.mockk:mockk-jvm")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

tasks {
    asciidoctor {
        inputs.dir(project.extra["snippetsDir"]!!)
        dependsOn(test)
        baseDirFollowsSourceDir()
        jvm {
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
        outputs.dir(project.extra["snippetsDir"]!!)
    }
}

asciidoctorj {
    fatalWarnings("include file not found") // make build fail if generated files are missing
    modules {
        diagram.use()
        diagram.setVersion("2.2.13")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

detekt {
    allRules = true
    buildUponDefaultConfig = true
    config.from("configurations/detekt.yml")
}

configurations.matching { it.name.startsWith("detekt") }
    .all {
        resolutionStrategy.eachDependency {
            // Detekt uses another Kotlin version internally
            if (requested.group == "org.jetbrains.kotlin")
                useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
