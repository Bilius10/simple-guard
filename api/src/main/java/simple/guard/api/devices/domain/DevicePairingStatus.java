package simple.guard.api.devices.domain;

public enum DevicePairingStatus {
    UNPAIRED("unpaired");

    private final String apiValue;

    DevicePairingStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}
