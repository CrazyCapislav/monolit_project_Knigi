package dev.petr.bookswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.dto.BookResponse;
import dev.petr.bookswap.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerMvcTest {

    private final BookService svc = Mockito.mock(BookService.class);
    private final MockMvc mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new BookController(svc))
            .setControllerAdvice(new dev.petr.bookswap.exception.GlobalExceptionHandler())
            .build();
    private final ObjectMapper om = new ObjectMapper();

    @Test
    void createBookOk() throws Exception {
        when(svc.create(anyLong(), any())).thenReturn(
                new BookResponse(1L, "Clean Code", "Robert Martin", null, 2008,
                        "AVAILABLE", "GOOD", OffsetDateTime.now(), 100L, Set.of()));

        var req = new BookCreateRequest("Clean Code", "Robert Martin", null, 2008, "GOOD", Set.of());

        mvc.perform(post("/api/v1/books")
                        .header("X-User-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void createBookInvalidTitle() throws Exception {
        var badReq = new BookCreateRequest("", "Author", null, 2020, "GOOD", Set.of());

        mvc.perform(post("/api/v1/books")
                        .header("X-User-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void pageBooks() throws Exception {
        var book1 = new BookResponse(1L, "Book1", "Author1", null, 2020,
                "AVAILABLE", "GOOD", OffsetDateTime.now(), 100L, Set.of());
        var book2 = new BookResponse(2L, "Book2", "Author2", null, 2021,
                "AVAILABLE", "NEW", OffsetDateTime.now(), 100L, Set.of());

        when(svc.findAll(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(book1, book2), 
                        org.springframework.data.domain.PageRequest.of(0, 20), 2));

        mvc.perform(get("/api/v1/books?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book1"));
    }

    @Test
    void feedBooks() throws Exception {
        var book = new BookResponse(10L, "BookFeed", "Author", null, 2022,
                "AVAILABLE", "GOOD", OffsetDateTime.now(), 100L, Set.of());

        when(svc.feed(anyLong(), anyInt())).thenReturn(List.of(book));

        mvc.perform(get("/api/v1/books/feed?after=100&limit=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void feedBooksWithoutTotalCount() throws Exception {
        when(svc.feed(isNull(), anyInt())).thenReturn(List.of());

        mvc.perform(get("/api/v1/books/feed?limit=10"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Total-Count"));
    }

    @Test
    void createBookInvalidYear() throws Exception {
        var badReq = new BookCreateRequest("Title", "Author", null, 3000, "GOOD", Set.of());

        mvc.perform(post("/api/v1/books")
                        .header("X-User-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest());
    }
}

