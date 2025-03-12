import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.named<BootJar>("bootJar") {
    isEnabled = true
}

tasks.named<Jar>("jar") {
    isEnabled = true
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // db
    runtimeOnly("com.mysql:mysql-connector-j")
    testRuntimeOnly("com.h2database:h2")

    // swagger
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Kotest BOM을 추가
    testImplementation(platform("io.kotest:kotest-bom:5.6.2"))

    testImplementation("io.kotest:kotest-property")

    // Kotest runner와 assertion
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")

    // Spring 환경에서 통합 테스트를 진행한다면
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")

}
