package dev.petr.publication.repository;

import dev.petr.publication.entity.PublicationRequest;
import dev.petr.publication.entity.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRequestRepository extends JpaRepository<PublicationRequest, Long> {

    Page<PublicationRequest> findAllByStatus(PublicationStatus status, Pageable pageable);

    Page<PublicationRequest> findAllByRequesterId(Long requesterId, Pageable pageable);
}