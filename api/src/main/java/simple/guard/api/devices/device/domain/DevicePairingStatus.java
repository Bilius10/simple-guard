package simple.guard.api.devices.device.domain;

public enum DevicePairingStatus {
    UNPAIRED("unpaired"),
    PAIRED("paired");

    private final String apiValue;

    DevicePairingStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}
