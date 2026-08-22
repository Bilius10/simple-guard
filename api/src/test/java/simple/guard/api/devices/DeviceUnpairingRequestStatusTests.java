package simple.guard.api.devices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;

class DeviceUnpairingRequestStatusTests {

  @Test
  void parsesEveryApiValueIgnoringCaseTests() {
    for (DeviceUnpairingRequestStatus status : DeviceUnpairingRequestStatus.values()) {
      assertThat(DeviceUnpairingRequestStatus.fromApiValue(status.apiValue())).isSameAs(status);
      assertThat(DeviceUnpairingRequestStatus.fromApiValue(status.name())).isSameAs(status);
    }
  }

  @Test
  void returnsNullWhenApiValueIsNullTests() {
    assertThat(DeviceUnpairingRequestStatus.fromApiValue(null)).isNull();
  }

  @Test
  void rejectsUnknownApiValueWithTranslationKeyTests() {
    assertThatThrownBy(() -> DeviceUnpairingRequestStatus.fromApiValue("unknown"))
        .isInstanceOf(SimpleGuardException.class)
        .hasMessage(SimpleGuardErrorCode.VALIDATION_ERROR.name());
  }
}
