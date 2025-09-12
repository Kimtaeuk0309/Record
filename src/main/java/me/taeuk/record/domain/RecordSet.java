package me.taeuk.record.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "records", columnDefinition = "LONGTEXT")
    private String records;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Builder
    public RecordSet(String records, LocalDateTime timestamp) {
        this.records = records;
        this.timestamp = timestamp;
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
