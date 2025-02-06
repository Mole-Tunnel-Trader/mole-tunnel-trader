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
        implementation(project(":mole-tunnel-db"))

        implementation(project(":webclient"))
        implementation(project(":common"))
    }
}

project(":mole-tunnel-db") {
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

// 복사 및 빌드 관련 작업 정의 코드
// 모듈 추가 되면 여기도 추가
val targetModules = listOf("mole-tunnel-db", "webclient")
val sourceResourcesPath = "kis-server/src/main/resources"
val destinationResourcesPath = "src/main/resources"

targetModules.forEach { moduleName ->
    tasks.register<Copy>("copyYmlFilesTo$moduleName") {
        description = "모듈 $moduleName 에 yml 및 xml 파일 복사"
        group = "my tasks"

        val modulePath = file("$moduleName/$destinationResourcesPath")

        // 디렉터리 생성
        doFirst {
            if (!modulePath.exists()) {
                modulePath.mkdirs()
            }
        }

        // 파일 복사
        from(file(sourceResourcesPath)) {
            include("*.yml", "*.xml")
        }
        into(modulePath)

        // 기존에 파일이 있으면 덮어쓰기
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

subprojects {
    if (project.name in targetModules) {
        tasks.matching { it.name == "build" }.configureEach {
            // 'build' 작업이 실행되기 전에 'copyYmlFilesTo<module>' 작업이 실행되도록 설정
            dependsOn(rootProject.tasks.named("copyYmlFilesTo${project.name}"))
        }

        // 'run' 작업을 kis-server 모듈에만 설정하도록
        if (project.name == "kis-server") {
            tasks.named("run").configure {
                dependsOn(targetModules.map { rootProject.tasks.named("copyYmlFilesTo$it") })
            }
        }

        // 'processResources' 작업이 실행되기 전에 yml 파일 복사를 완료하도록 보장
        tasks.matching { it.name == "processResources" }.configureEach {
            dependsOn(rootProject.tasks.named("copyYmlFilesTo${project.name}"))
        }
    }
}
