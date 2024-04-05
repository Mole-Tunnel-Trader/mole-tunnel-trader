import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.named<BootJar>("bootJar") {
    isEnabled = false
}

tasks.named<Jar>("jar") {
    isEnabled = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // logback & webclient
    implementation("io.netty:netty-all") // mac
    implementation("io.micrometer:micrometer-core") // mac
}
