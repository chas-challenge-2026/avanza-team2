package se.comerit.avanza.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.entity.PositionSnapshot;

public interface PositionSnapshotRepository extends JpaRepository<PositionSnapshot, Long> {
    boolean existsByUserIdAndPriceDate(Long userId, LocalDate priceDate);
}