plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "simple.guard.agent"
    compileSdk = 35

    defaultConfig {
        applicationId = "simple.guard.agent"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.json:json:20240303")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.Manifest",
                    "*.Manifest.*",
                    "*.R",
                    "*.R.*",
                    "*MainActivity*",
                    "*LocalPairing*",
                    "*AgentKeyStore*",
                    "*PairingApiClient*",
                    "*PairingApiException*",
                    "*UnpairingApiClient*",
                    "*UnpairingApiException*",
                    "*DeviceUnpairingRequestResponse*",
                    "*AgentPairingStatusResponse*",
                    "*AndroidLocationCollector*",
                    "*LocationApiClient*",
                    "*LocationApiException*",
                    "*LocationApiSender*",
                    "*LocationTrackingService*",
                    "*LocationDiagnosticsStore*",
                    "*LocationDiagnosticStatus*",
                    "*LocationDiagnosticsSnapshot*"
                )
            }
        }

        verify {
            rule {
                minBound(100)
            }
        }
    }
}
