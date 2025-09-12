package me.taeuk.record.repository;

import me.taeuk.record.domain.RecordData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordDataRepository extends JpaRepository<RecordData, Long> {
}
