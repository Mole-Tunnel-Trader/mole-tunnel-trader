import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.named<BootJar>("bootJar") {
    isEnabled = false
}

tasks.named<Jar>("jar") {
    isEnabled = true
}

dependencies {
//    api("org.springframework.boot:spring-boot-starter-webflux")
    api("com.squareup.okhttp3:okhttp:4.12.0")

    // logback & webclient
    api("io.netty:netty-all") // mac
    api("io.micrometer:micrometer-core") // mac
}
