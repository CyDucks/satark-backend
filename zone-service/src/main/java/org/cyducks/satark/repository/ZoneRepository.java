package org.cyducks.satark.repository;

import org.cyducks.satark.data.ConflictZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneRepository extends JpaRepository<ConflictZone, String> {
    List<ConflictZone> findByActiveTrue();
}