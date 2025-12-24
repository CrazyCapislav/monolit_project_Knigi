package dev.petr.book.controller;

import dev.petr.book.dto.BookCreateRequest;
import dev.petr.book.dto.BookResponse;
import dev.petr.book.dto.UpdateBookOwnerRequest;
import dev.petr.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebFluxTest(BookController.class)
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @Test
    void create_Success() {
        BookCreateRequest request = new BookCreateRequest(
                "Test Book",
                "Test Author",
                "978-1234567890",
                2024,
                "GOOD",
                Set.of(1L)
        );

        BookResponse response = new BookResponse(
                1L,
                "Test Book",
                "Test Author",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                1L,
                Set.of("Fiction")
        );

        when(bookService.create(anyLong(), any(BookCreateRequest.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/books")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Book")
                .jsonPath("$.author").isEqualTo("Test Author");
    }

    @Test
    void getById_Success() {
        BookResponse response = new BookResponse(
                1L,
                "Test Book",
                "Test Author",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                1L,
                Set.of("Fiction")
        );

        when(bookService.findById(1L)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/books/1")
                .header("X-User-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Test Book");
    }

    @Test
    void page_Success() {
        BookResponse response1 = new BookResponse(
                1L,
                "Book 1",
                "Author 1",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                1L,
                Set.of("Fiction")
        );

        BookResponse response2 = new BookResponse(
                2L,
                "Book 2",
                "Author 2",
                "978-0987654321",
                2023,
                "AVAILABLE",
                "EXCELLENT",
                OffsetDateTime.now(),
                2L,
                Set.of("Mystery")
        );

        when(bookService.findAll(anyInt(), anyInt()))
                .thenReturn(Mono.just(new PageImpl<>(Arrays.asList(response1, response2))));

        webTestClient.get()
                .uri("/api/v1/books?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Total-Count");
    }

    @Test
    void feed_Success() {
        BookResponse response = new BookResponse(
                1L,
                "Test Book",
                "Test Author",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                1L,
                Set.of("Fiction")
        );

        when(bookService.feed(any(), anyInt()))
                .thenReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/v1/books/feed?limit=20")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookResponse.class)
                .hasSize(1);
    }

    @Test
    void getMyBooks_Success() {
        BookResponse response = new BookResponse(
                1L,
                "My Book",
                "Test Author",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                1L,
                Set.of("Fiction")
        );

        when(bookService.findByOwnerId(1L))
                .thenReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/v1/books/mybooks")
                .header("X-User-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookResponse.class)
                .hasSize(1);
    }

    @Test
    void delete_Success() {
        when(bookService.delete(1L, 1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/books/1")
                .header("X-User-Id", "1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateOwner_Success() {
        UpdateBookOwnerRequest request = new UpdateBookOwnerRequest(2L);

        BookResponse response = new BookResponse(
                1L,
                "Test Book",
                "Test Author",
                "978-1234567890",
                2024,
                "AVAILABLE",
                "GOOD",
                OffsetDateTime.now(),
                2L,
                Set.of("Fiction")
        );

        when(bookService.updateOwner(1L, 1L, 2L))
                .thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/books/1/owner")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.owner_id").isEqualTo(2);
    }
}

