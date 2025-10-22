package dev.petr.bookswap.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler h = new GlobalExceptionHandler();

    @Test
    void notFoundMapsTo404() {
        var resp = h.handleNotFound(new NotFoundException("x"),
                new MockHttpServletRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(resp.getBody()).status()).isEqualTo(404);
    }

    @Test
    void illegalStateMapsTo409() {
        var resp = h.handleIllegal(new IllegalStateException("boom"),
                new MockHttpServletRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(Objects.requireNonNull(resp.getBody()).status()).isEqualTo(409);
    }
}
