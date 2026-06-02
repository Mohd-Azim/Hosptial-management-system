package com.hospital.hms.repo;

import com.hospital.hms.domain.PatientProfile;
import com.hospital.hms.web.dto.PatientSearchHit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUser_Id(Long userId);

    @Query("select new com.hospital.hms.web.dto.PatientSearchHit(u.id, u.fullName, p.mrn) "
            + "from PatientProfile p join p.user u "
            + "where u.nameBucket = :bucket and u.sortNameNorm like concat(:prefix, '%') "
            + "order by u.sortNameNorm")
    List<PatientSearchHit> searchByBucketAndPrefix(
            @Param("bucket") String bucket,
            @Param("prefix") String prefix,
            Pageable page);
}
