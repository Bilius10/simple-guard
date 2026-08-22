package simple.guard.api.devices.pairingsession.domain;

public enum PairingSessionStatus {
  WAITING("waiting"),
  USED("used"),
  EXPIRED("expired");

  private final String apiValue;

  PairingSessionStatus(String apiValue) {
    this.apiValue = apiValue;
  }

  public String apiValue() {
    return apiValue;
  }
}
