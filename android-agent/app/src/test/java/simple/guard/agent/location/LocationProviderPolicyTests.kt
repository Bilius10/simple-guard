package simple.guard.agent.location

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationProviderPolicyTests {
    @Test
    fun exposesSupportedProvidersInExpectedOrderTests() {
        val providers = LocationProviderPolicy.supportedProviders()

        assertEquals(listOf("GPS", "FUSED", "NETWORK"), providers.map { it.contractName })
    }

    @Test
    fun prefersGpsBeforeFusedAndNetworkWhenPrecisePermissionIsGrantedTests() {
        val providers =
            LocationProviderPolicy.preferredProviders(
                enabledProviderNames = linkedSetOf("network", "gps", "fused"),
                precisePermissionGranted = true,
            )

        assertEquals(listOf("GPS", "FUSED", "NETWORK"), providers.map { it.contractName })
    }

    @Test
    fun skipsGpsWhenOnlyApproximatePermissionIsGrantedTests() {
        val providers =
            LocationProviderPolicy.preferredProviders(
                enabledProviderNames = linkedSetOf("network", "gps", "fused"),
                precisePermissionGranted = false,
            )

        assertEquals(listOf("FUSED", "NETWORK"), providers.map { it.contractName })
    }

    @Test
    fun returnsOnlyEnabledProvidersInPreferredOrderTests() {
        val providers =
            LocationProviderPolicy.preferredProviders(
                enabledProviderNames = linkedSetOf("network"),
                precisePermissionGranted = true,
            )

        assertEquals(listOf("NETWORK"), providers.map { it.contractName })
    }
}
