package dev.petr.bookswap.repository;
import dev.petr.bookswap.entity.PublicationRequest;
import dev.petr.bookswap.entity.PublicationStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRequestRepository extends JpaRepository<PublicationRequest, Long> {
    Page<PublicationRequest> findAllByStatus(PublicationStatus status, Pageable pageable);
}
