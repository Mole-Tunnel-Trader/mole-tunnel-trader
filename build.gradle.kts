import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    kotlin("kapt") version "1.9.23"
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
}


allprojects {
    group = "com.zeki"
    version = "0.0.1-SNAPSHOT"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.kapt")


    allOpen {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }

    dependencies {
        // spring boot
        implementation("org.springframework.boot:spring-boot-starter-web")
        // kotlin
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }
}

project(":kis-server") {
    dependencies {
        implementation(project(":stock-data"))
        implementation(project(":holiday"))
        implementation(project(":stock-code"))
        implementation(project(":token"))
        implementation(project(":trade"))

        implementation(project(":webclient"))
        implementation(project(":common"))
    }
}


project(":stock-data") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":webclient"))
    }
}

project(":holiday") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":webclient"))
    }
}

project(":stock-code") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":webclient"))
    }
}

project(":token") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":webclient"))
    }
}

project(":trade") {
    dependencies {
        implementation(project(":common"))
    }
}

project(":webclient") {
    dependencies {
        implementation(project(":common"))
    }
}

project(":common") {
    dependencies {

    }
}


// subModule
tasks.register<Copy>("copyYmlFiles") {
    description = "yml 파일 복사"
    group = "my tasks"
    from("kis-vol-kotlin-yml")
    into("kis-server/src/main/resources")
}