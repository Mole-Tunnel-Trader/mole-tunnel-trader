plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kis-vol-kotlin"

include(
    "kis-server",
    "stock-data",
    "holiday",
    "stock-code",
    "token",
    "trade",


    "common",
    "webclient",
)
include("elphago")
