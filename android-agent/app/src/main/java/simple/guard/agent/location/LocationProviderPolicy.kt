package simple.guard.agent.location

data class LocationProviderCandidate(
    val systemName: String,
    val contractName: String,
)

object LocationProviderPolicy {
    private val providers =
        listOf(
            LocationProviderCandidate("gps", "GPS"),
            LocationProviderCandidate("fused", "FUSED"),
            LocationProviderCandidate("network", "NETWORK"),
        )

    fun supportedProviders(): List<LocationProviderCandidate> = providers

    fun preferredProviders(
        enabledProviderNames: Set<String>,
        precisePermissionGranted: Boolean,
    ): List<LocationProviderCandidate> {
        return providers.filter { provider ->
            enabledProviderNames.contains(provider.systemName) &&
                (precisePermissionGranted || provider.contractName != "GPS")
        }
    }
}
