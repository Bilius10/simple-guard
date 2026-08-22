package simple.guard.api.error.domain;

import org.springframework.http.HttpStatus;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

public class SimpleGuardException extends RuntimeException {

  private final HttpStatus status;
  private final SimpleGuardErrorCode errorCode;
  private final SimpleGuardTranslation translation;

  public SimpleGuardException(
      HttpStatus status, SimpleGuardErrorCode errorCode, SimpleGuardTranslation translation) {
    super(errorCode.name());
    this.status = status;
    this.errorCode = errorCode;
    this.translation = translation;
  }

  public HttpStatus status() {
    return status;
  }

  public SimpleGuardErrorCode errorCode() {
    return errorCode;
  }

  public SimpleGuardTranslation translation() {
    return translation;
  }
}
