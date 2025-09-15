package dev.petr.bookswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.petr.bookswap.dto.UserCreateRequest;
import dev.petr.bookswap.dto.UserResponse;
import dev.petr.bookswap.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerMvcTest {

    private final UserService svc = Mockito.mock(UserService.class);
    private final MockMvc mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(new UserController(svc))
            .setControllerAdvice(new dev.petr.bookswap.exception.GlobalExceptionHandler())
            .build();
    private final ObjectMapper om = new ObjectMapper();

    @Test void register_ok() throws Exception {
        when(svc.register(any())).thenReturn(
                new UserResponse(1L, "valid@mail.com", "Valid Name", "USER")
        );

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new UserCreateRequest("valid@mail.com", "Valid Name", "securePassword123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test void register_validationError() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new UserCreateRequest("bad-mail","X","p"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
