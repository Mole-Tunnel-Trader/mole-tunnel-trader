import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.named<BootJar>("bootJar") {
    isEnabled = true
}

tasks.named<Jar>("jar") {
    isEnabled = true
}

//tasks.test {
//    useJUnitPlatform()
//}

dependencies {
    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // db
    runtimeOnly("com.mysql:mysql-connector-j")
    testRuntimeOnly("com.h2database:h2")

    // swagger
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2") // 최신 버전 확인

}
