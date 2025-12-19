package dev.petr.exchange.controller;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleIllegalArgument_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().get("message"));
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void handleValidation_WithFieldError_ReturnsFormattedMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("field"));
        assertTrue(response.getBody().get("message").toString().contains("must not be null"));
    }

    @Test
    void handleValidation_WithNoFieldError_ReturnsDefaultMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().get("message"));
    }

    @Test
    void handleFeignException_BadRequest_ReturnsBadRequest() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.BadRequest ex = new FeignException.BadRequest("Bad request", feignRequest, null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleFeignException_NotFound_ReturnsNotFound() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound ex = new FeignException.NotFound("Not found", feignRequest, null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleFeignException_Forbidden_ReturnsForbidden() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.Forbidden ex = new FeignException.Forbidden("Forbidden", feignRequest, null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleFeignException_Unauthorized_ReturnsUnauthorized() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.Unauthorized ex = new FeignException.Unauthorized("Unauthorized", feignRequest, null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleFeignException_ServerError_ReturnsServiceUnavailable() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException ex = new FeignException.InternalServerError("Server error", feignRequest, null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleGeneric_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
    }
}

