package dev.petr.bookswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.PublicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicationControllerMvcTest {

    private final PublicationService svc = Mockito.mock(PublicationService.class);
    private final MockMvc mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new PublicationController(svc))
            .build();
    private final ObjectMapper om = new ObjectMapper();

    @Test void approve_ok() throws Exception {
        when(svc.decide(3L,9L,true)).thenReturn(
                new PublicationRequestResponse(3L,1L,9L,"T","A",
                        "m","APPROVED",OffsetDateTime.now(),OffsetDateTime.now())
        );
        mvc.perform(post("/api/v1/publications/3/approve")
                        .header("X-User-Id","9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test void create_validationError() throws Exception {
        var bad = new PublicationRequestCreateRequest("","A",null,9L);
        mvc.perform(post("/api/v1/publications")
                        .header("X-User-Id","1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }
}
