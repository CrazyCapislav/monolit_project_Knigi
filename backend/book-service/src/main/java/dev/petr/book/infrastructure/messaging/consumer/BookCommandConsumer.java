package dev.petr.book.infrastructure.messaging.consumer;

import dev.petr.book.application.dto.BookCreateRequest;
import dev.petr.book.application.dto.BookResponse;
import dev.petr.book.application.service.BookService;
import dev.petr.book.domain.event.BookCommandEvent;
import dev.petr.book.domain.event.BookDomainEvent;
import dev.petr.book.infrastructure.messaging.producer.BookEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookCommandConsumer {

    private final BookService bookService;
    private final BookEventProducer bookEventProducer;

    @KafkaListener(
            topics = "book-command-events",
            groupId = "book-service",
            containerFactory = "bookCommandKafkaListenerContainerFactory"
    )
    public void consume(BookCommandEvent command) {
        if (command == null || command.getCommandType() == null) {
            log.warn("Received empty book command");
            return;
        }

        switch (command.getCommandType()) {
            case "CREATE_BOOK" -> handleCreate(command);
            case "UPDATE_OWNER" -> handleOwnerUpdate(command);
            default -> log.warn("Unknown book command type: {}", command.getCommandType());
        }
    }

    private void handleCreate(BookCommandEvent command) {
        try {
            BookCreateRequest request = new BookCreateRequest(
                    command.getTitle(),
                    command.getAuthor(),
                    command.getIsbn(),
                    command.getPublishedYear(),
                    command.getCondition() != null ? command.getCondition() : "NEW",
                    command.getGenreIds()
            );

            BookResponse created = bookService.create(command.getOwnerId(), request).block();
            if (created == null) {
                log.warn("Book creation returned null for publication {}", command.getPublicationRequestId());
                return;
            }

            BookDomainEvent event = BookDomainEvent.builder()
                    .bookId(created.id())
                    .ownerId(created.ownerId())
                    .publicationRequestId(command.getPublicationRequestId())
                    .build();
            bookEventProducer.sendBookCreated(event);
        } catch (Exception e) {
            log.error("Error handling CREATE_BOOK command", e);
        }
    }

    private void handleOwnerUpdate(BookCommandEvent command) {
        try {
            bookService.updateOwner(
                    command.getBookId(),
                    command.getCurrentOwnerId(),
                    command.getNewOwnerId()
            ).block();

            BookDomainEvent event = BookDomainEvent.builder()
                    .bookId(command.getBookId())
                    .ownerId(command.getNewOwnerId())
                    .build();
            bookEventProducer.sendOwnerUpdated(event);
        } catch (Exception e) {
            log.error("Error handling UPDATE_OWNER command", e);
        }
    }
}
