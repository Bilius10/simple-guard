package simple.guard.api.devices.deviceunpairingrequest.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.util.Arrays;

public enum DeviceUnpairingRequestStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String apiValue;

    DeviceUnpairingRequestStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String apiValue() {
        return apiValue;
    }

    @JsonCreator
    public static DeviceUnpairingRequestStatus fromApiValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(status -> status.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new SimpleGuardException(HttpStatus.BAD_REQUEST,
                        SimpleGuardErrorCode.VALIDATION_ERROR,
                        SimpleGuardTranslation.INVALID_STATUS_MESSAGE));
    }
}

