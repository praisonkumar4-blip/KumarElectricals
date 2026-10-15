package in.kumarelectrical.contractor.state;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="application_state")
public class AppStateEntity {
    @Id @Column(name="state_key", length=80) private String keyName;
    @Lob @Column(name="payload", columnDefinition="LONGTEXT", nullable=false) private String payload;
    @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
    public AppStateEntity() {}
    public AppStateEntity(String keyName, String payload) { this.keyName=keyName; this.payload=payload; this.updatedAt=Instant.now(); }
    public String getKeyName(){return keyName;} public void setKeyName(String v){keyName=v;}
    public String getPayload(){return payload;} public void setPayload(String v){payload=v; updatedAt=Instant.now();}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
