import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    kotlin("kapt") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.zeki"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

//    tasks.withType<Test> {
//        useJUnitPlatform()
//    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    dependencies {
        // spring boot
        api("org.springframework.boot:spring-boot-starter-web")
        // kotlin
        api("com.fasterxml.jackson.module:jackson-module-kotlin")
        api("org.jetbrains.kotlin:kotlin-reflect")
    }
}

project(":kis-server") {
    dependencies {
        implementation(project(":report"))
        implementation(project(":holiday"))

        implementation(project(":mole-tunnel-db"))
        implementation(project(":ok-http-client"))
        implementation(project(":common"))
    }
}

project(":back-test-server") {
    dependencies {
        implementation(project(":report"))
        implementation(project(":holiday"))

        implementation(project(":mole-tunnel-db"))
        implementation(project(":ok-http-client"))
        implementation(project(":algorithm-common"))
        implementation(project(":common"))

        implementation(project(":_test_algo"))
    }
}

project(":report") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":mole-tunnel-db"))
        implementation(project(":ok-http-client"))
    }
}

project(":holiday") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":mole-tunnel-db"))
        implementation(project(":ok-http-client"))
    }
}

project(":mole-tunnel-db") {
    dependencies {
        implementation(project(":common"))
    }
}


project(":ok-http-client") {
    dependencies {
        implementation(project(":common"))
    }
}

project(":_test_algo") {
    dependencies {
        implementation(project(":common"))
        implementation(project(":algorithm-common"))
    }
}

project(":algorithm-common") {
    dependencies {
        implementation(project(":common"))
    }
}

project(":common") {
    dependencies {

    }
}