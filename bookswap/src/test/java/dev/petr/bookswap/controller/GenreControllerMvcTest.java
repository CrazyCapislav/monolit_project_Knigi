package dev.petr.bookswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.bookswap.dto.GenreCreateRequest;
import dev.petr.bookswap.dto.GenreResponse;
import dev.petr.bookswap.service.GenreService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GenreControllerMvcTest {

    private final GenreService svc = Mockito.mock(GenreService.class);
    private final MockMvc mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new GenreController(svc))
            .build();
    private final ObjectMapper om = new ObjectMapper();

    @Test
    void createAndList() throws Exception {
        when(svc.create(any())).thenReturn(new GenreResponse(7L, "Sci-Fi"));
        when(svc.findAll()).thenReturn(List.of(new GenreResponse(7L, "Sci-Fi")));

        // create
        mvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new GenreCreateRequest("Sci-Fi"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sci-Fi"));

        // list
        mvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sci-Fi"));
    }
}
