plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
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

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    testImplementation(kotlin("test"))
    testImplementation("org.json:json:20240303")
}

kover {
    reports {
        variant("debug") {
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
                        "*AndroidTechnicalTelemetryCollector*",
                        "*LocationApiClient*",
                        "*LocationApiException*",
                        "*LocationApiSender*",
                        "*FileTelemetryOfflineQueue*",
                        "*TelemetryJsonCodec*",
                        "*LocationTrackingService*",
                        "*LocationDiagnosticsStore*",
                        "*LocationDiagnosticStatus*",
                        "*LocationDiagnosticsSnapshot*",
                        "*AgentPreferencesStore*",
                        "*AgentScreenTheme*",
                        "*BaseScreenFactory*",
                        "*WelcomeScreenFactory*",
                        "*PairingScreenFactory*",
                        "*PairingScreenRenderer*",
                        "*PairingScreenViews*",
                        "*UnpairingScreenFactory*",
                        "*UnpairingScreenRenderer*",
                        "*UnpairingScreenViews*",
                        "*DiagnosticsScreenFactory*",
                        "*DiagnosticsScreenRenderer*",
                        "*DiagnosticsScreenViews*",
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
}
