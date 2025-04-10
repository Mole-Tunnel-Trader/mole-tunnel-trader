plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kis-vol-kotlin"

include(
    "kis-server",
    "common",
    "ok-http-client",
)
include("mole-tunnel-db")
include("back-test-server")
include("report")
include("algorithm-common")
include("holiday")
include("cursor-algorithm")
include("stock-code")
include("stock-data")
