package dev.petr.publication.infrastructure.persistence.repository;

import dev.petr.publication.domain.model.PublicationRequest;
import dev.petr.publication.domain.model.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRequestRepository extends JpaRepository<PublicationRequest, Long> {

    Page<PublicationRequest> findAllByStatus(PublicationStatus status, Pageable pageable);

    Page<PublicationRequest> findAllByRequesterId(Long requesterId, Pageable pageable);
}
