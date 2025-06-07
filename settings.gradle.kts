plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "bank-server"

include(":bank-api")
include(":bank-core")
include(":bank-domain")
include(":bank-event")