package dev.petr.exchange.controller;

import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.service.ExchangeService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeService exchangeService;

    @Test
    void create_Success() throws Exception {
        ExchangeRequestResponse response = new ExchangeRequestResponse(
                1L,
                1L,
                2L,
                1L,
                2L,
                "PENDING",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(exchangeService.create(anyLong(), any(ExchangeRequestCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/exchanges")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"book_requested_id\":1,\"book_offered_id\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book_requested_id").value(1))
                .andExpect(jsonPath("$.book_offered_id").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void accept_Success() throws Exception {
        ExchangeRequestResponse response = new ExchangeRequestResponse(
                1L,
                1L,
                2L,
                1L,
                2L,
                "ACCEPTED",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(exchangeService.accept(anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/exchanges/1/accept")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void page_Success() throws Exception {
        ExchangeRequestResponse response1 = new ExchangeRequestResponse(
                1L,
                1L,
                2L,
                1L,
                2L,
                "PENDING",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        ExchangeRequestResponse response2 = new ExchangeRequestResponse(
                2L,
                3L,
                4L,
                3L,
                4L,
                "ACCEPTED",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(exchangeService.page(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Arrays.asList(response1, response2)));

        mockMvc.perform(get("/api/v1/exchanges?page=0&size=20")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void page_WithCustomPagination() throws Exception {
        ExchangeRequestResponse response = new ExchangeRequestResponse(
                1L, 1L, 2L, 1L, 2L, "PENDING",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(exchangeService.page(2, 10))
                .thenReturn(new PageImpl<>(Arrays.asList(response)));

        mockMvc.perform(get("/api/v1/exchanges?page=2&size=10")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void page_WithDefaultPagination() throws Exception {
        when(exchangeService.page(0, 20))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        mockMvc.perform(get("/api/v1/exchanges")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }
}
