package simple.guard.api.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "accounts")
public class Account {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String subject;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String displayName;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private boolean active;

  @Column(nullable = false)
  @CreatedBy
  private String createdBy;

  @Column(nullable = false)
  @CreatedDate
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  @LastModifiedBy
  private String updatedBy;

  @Column(nullable = false)
  @LastModifiedDate
  private OffsetDateTime updatedAt;

  protected Account() {}
}
