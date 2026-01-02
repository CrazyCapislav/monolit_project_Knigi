package dev.petr.exchange.repository;

import dev.petr.exchange.entity.ExchangeRequest;
import dev.petr.exchange.entity.ExchangeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    
    Page<ExchangeRequest> findAllByStatus(ExchangeStatus status, Pageable pageable);

    Page<ExchangeRequest> findByRequesterIdOrOwnerId(Long requesterId, Long ownerId, Pageable pageable);
}
