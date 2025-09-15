package dev.petr.bookswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.bookswap.dto.ExchangeRequestCreateRequest;
import dev.petr.bookswap.dto.ExchangeRequestResponse;
import dev.petr.bookswap.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExchangeControllerMvcTest {

    private final ExchangeService svc = Mockito.mock(ExchangeService.class);
    private final MockMvc mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new ExchangeController(svc))
            .build();
    private final ObjectMapper om = new ObjectMapper();

    @Test void accept_ok() throws Exception {
        when(svc.accept(5L,2L)).thenReturn(
                new ExchangeRequestResponse(5L,1L,2L,10L,null,
                        "ACCEPTED", OffsetDateTime.now(),OffsetDateTime.now())
        );
        mvc.perform(post("/api/v1/exchanges/5/accept")
                        .header("X-User-Id","2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test void create_ok() throws Exception {
        when(svc.create(eq(1L), any())).thenReturn(
                new ExchangeRequestResponse(9L,1L,2L,10L,null,
                        "WAITING", OffsetDateTime.now(),null)
        );
        mvc.perform(post("/api/v1/exchanges")
                        .header("X-User-Id","1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new ExchangeRequestCreateRequest(10L,null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
