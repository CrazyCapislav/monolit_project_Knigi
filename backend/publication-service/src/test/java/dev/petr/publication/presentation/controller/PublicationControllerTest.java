package dev.petr.publication.presentation.controller;

import dev.petr.publication.application.dto.PublicationRequestCreateRequest;
import dev.petr.publication.application.dto.PublicationRequestResponse;
import dev.petr.publication.application.service.PublicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(PublicationController.class)
class PublicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicationService publicationService;

    @Test
    void create_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L,
                1L,
                "New Book",
                "New Author",
                "978-1234567890",
                2024,
                "GOOD",
                "PENDING",
                null,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(publicationService.requestBook(anyLong(), any(PublicationRequestCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/publications/request")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Book\",\"author\":\"New Author\",\"isbn\":\"978-1234567890\",\"published_year\":2024,\"description\":\"GOOD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void approve_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L,
                1L,
                "New Book",
                "New Author",
                "978-1234567890",
                2024,
                "GOOD",
                "APPROVED",
                2L,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(publicationService.approve(anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/publications/1/approve")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_PUBLISHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void reject_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L,
                1L,
                "New Book",
                "New Author",
                "978-1234567890",
                2024,
                "GOOD",
                "REJECTED",
                2L,
                null,
                "Not suitable",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(publicationService.reject(anyLong(), anyLong(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/publications/1/reject")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_PUBLISHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Not suitable\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void publish_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L,
                1L,
                "New Book",
                "New Author",
                "978-1234567890",
                2024,
                "GOOD",
                "PUBLISHED",
                2L,
                10L,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(publicationService.publish(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/publications/1/publish")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_PUBLISHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genre_ids\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void page_Success() throws Exception {
        PublicationRequestResponse response1 = new PublicationRequestResponse(
                1L,
                1L,
                "Book 1",
                "Author 1",
                "978-1234567890",
                2024,
                "GOOD",
                "PENDING",
                null,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        PublicationRequestResponse response2 = new PublicationRequestResponse(
                2L,
                2L,
                "Book 2",
                "Author 2",
                "978-0987654321",
                2023,
                "EXCELLENT",
                "APPROVED",
                3L,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(publicationService.getAllRequests(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Arrays.asList(response1, response2)));

        mockMvc.perform(get("/api/v1/publications?page=0&size=20")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getPendingRequests_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L, 1L, "Book", "Author", "978-1234567890", 2024, "GOOD",
                "PENDING", null, null, null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(publicationService.getPendingRequests(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Arrays.asList(response)));

        mockMvc.perform(get("/api/v1/publications/pending?page=0&size=20")
                        .header("X-User-Role", "ROLE_PUBLISHER"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyRequests_Success() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L, 1L, "My Book", "Author", "978-1234567890", 2024, "GOOD",
                "PENDING", null, null, null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(publicationService.getMyRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Arrays.asList(response)));

        mockMvc.perform(get("/api/v1/publications/my?page=0&size=20")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void publish_WithEmptyGenreIds_ThrowsException() throws Exception {
        mockMvc.perform(post("/api/v1/publications/1/publish")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_PUBLISHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genre_ids\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reject_WithoutReason_UsesDefault() throws Exception {
        PublicationRequestResponse response = new PublicationRequestResponse(
                1L, 1L, "Book", "Author", "978-1234567890", 2024, "GOOD",
                "REJECTED", 2L, null, "Not specified", OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(publicationService.reject(anyLong(), anyLong(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/publications/1/reject")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_PUBLISHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}

