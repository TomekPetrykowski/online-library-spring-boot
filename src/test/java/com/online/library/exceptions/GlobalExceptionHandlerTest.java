package com.online.library.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler underTest;

    @BeforeEach
    void setUp() {
        underTest = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFoundExceptionReturns404() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Book not found");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/books/999");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest
                .handleResourceNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Book not found");
        assertThat(response.getBody().details()).isEqualTo("uri=/api/v1/books/999");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void testHandleResourceNotFoundExceptionWithDifferentEntity() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User with id 123 not found");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/users/123");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest
                .handleResourceNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("User");
        assertThat(response.getBody().message()).contains("123");
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionReturnsBadRequest() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("bookDto", "title", "Title is required");
        FieldError fieldError2 = new FieldError("bookDto", "isbn", "ISBN must be valid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Object> response = underTest.handleMethodArgumentNotValidException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ValidationError> errors = (List<GlobalExceptionHandler.ValidationError>) response
                .getBody();

        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).field()).isEqualTo("title");
        assertThat(errors.get(0).message()).isEqualTo("Title is required");
        assertThat(errors.get(1).field()).isEqualTo("isbn");
        assertThat(errors.get(1).message()).isEqualTo("ISBN must be valid");
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithSingleError() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("userDto", "email", "Email should be valid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Object> response = underTest.handleMethodArgumentNotValidException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ValidationError> errors = (List<GlobalExceptionHandler.ValidationError>) response
                .getBody();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).field()).isEqualTo("email");
    }

    @Test
    void testHandleGlobalExceptionReturns500() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/books");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest.handleGlobalException(exception,
                webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected error occurred");
        assertThat(response.getBody().details()).isEqualTo("uri=/api/v1/books");
    }

    @Test
    void testHandleGlobalExceptionWithNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("null value");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/reservations");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest.handleGlobalException(exception,
                webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("null value");
    }

    @Test
    void testHandleGlobalExceptionWithIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/ratings");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest.handleGlobalException(exception,
                webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Invalid argument provided");
    }

    @Test
    void testErrorDetailsRecordContainsTimestamp() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorDetails> response = underTest
                .handleResourceNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().timestamp()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithEmptyErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        ResponseEntity<Object> response = underTest.handleMethodArgumentNotValidException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        List<GlobalExceptionHandler.ValidationError> errors = (List<GlobalExceptionHandler.ValidationError>) response
                .getBody();

        assertThat(errors).isEmpty();
    }
}
