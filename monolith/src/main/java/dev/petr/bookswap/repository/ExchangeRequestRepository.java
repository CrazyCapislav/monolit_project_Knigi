package dev.petr.bookswap.repository;
import dev.petr.bookswap.entity.ExchangeRequest;
import dev.petr.bookswap.entity.ExchangeStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    Page<ExchangeRequest> findAllByStatus(ExchangeStatus status, Pageable pageable);
}
