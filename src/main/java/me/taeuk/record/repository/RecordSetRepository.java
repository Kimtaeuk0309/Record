package me.taeuk.record.repository;

import me.taeuk.record.domain.RecordSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordSetRepository extends JpaRepository<RecordSet, Long> {


    @Query("SELECT r FROM RecordSet r ORDER BY r.id DESC")
    List<RecordSet> findLatestRecords(Pageable pageable);

    default Optional<RecordSet> findTopRecordSet() {
        return findLatestRecords(PageRequest.of(0,1)).stream().findFirst();
    }

    List<RecordSet> findAllByOrderByIdDesc();

}
