package id.ac.ui.cs.advprog.auctionwallet.wallet.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
    "PMD.UnitTestShouldIncludeAssert",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnitTestAssertionsShouldIncludeMessage",
    "PMD.LawOfDemeter",
    "PMD.AvoidDuplicateLiterals"
})
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleInsufficientBalanceReturnsBadRequest() {
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<Map<String, String>> response = handler.handleInsufficientBalance(ex);
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient balance", body.get("error"));
    }

    @Test
    void testHandleWalletNotFoundReturnsNotFound() {
        WalletNotFoundException ex = new WalletNotFoundException("Wallet not found");

        ResponseEntity<Map<String, String>> response = handler.handleWalletNotFound(ex);
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Wallet not found", body.get("error"));
    }

    @Test
    void testHandleIllegalArgumentReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Amount must be positive");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount must be positive", body.get("error"));
    }

    @Test
    void testHandleValidationExceptionsReturnsFieldErrors() {
        FieldError fieldError = new FieldError("topUpRequest", "amount", "must be positive");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(validationException);
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must be positive", body.get("amount"));
    }

    @Test
    void testHandleValidationExceptionsWithMultipleFields() {
        FieldError amountError = new FieldError("request", "amount", "must be positive");
        FieldError refError = new FieldError("request", "referenceId", "must not be blank");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(amountError, refError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(validationException);
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must be positive", body.get("amount"));
        assertEquals("must not be blank", body.get("referenceId"));
    }
}
