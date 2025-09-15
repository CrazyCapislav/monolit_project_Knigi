package dev.petr.bookswap.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler h = new GlobalExceptionHandler();

    @Test void notFound_mapsTo404() {
        var resp = h.handleNotFound(new NotFoundException("x"),
                new MockHttpServletRequest());
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().status()).isEqualTo(404);
    }
}
