plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "multi-module"

include("bank-api")
include("bank-domain")
include("bank-core")
include("bank-event")
include("bank-monitoring")