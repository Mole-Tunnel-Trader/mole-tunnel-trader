plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kis-vol-kotlin"

include(
    "kis-server",
    "common",
    "ok-http-client",
    "data-go"
)
include("mole-tunnel-db")
include("back-test-server")