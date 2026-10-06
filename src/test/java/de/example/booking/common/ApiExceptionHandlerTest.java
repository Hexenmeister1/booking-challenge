package de.example.booking.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ApiExceptionHandlerTest {

    @Test
        void reportsAllInvalidFieldsAndUsesFallbackMessages() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", null, false, null, null, null));
        bindingResult.addError(
                new FieldError("request", "name", null, false, null, null, "second error"));
        bindingResult.addError(
                new FieldError("request", "start", null, false, null, null, "must be in future"));

        Method handlerMethod =
                Objects.requireNonNull(
                        ApiExceptionHandler.class.getDeclaredMethod(
                                "handleValidationFailure", MethodArgumentNotValidException.class));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(new MethodParameter(handlerMethod, 0), bindingResult);

        ProblemDetail problem = new ApiExceptionHandler().handleValidationFailure(exception);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Ungültige Eingabe");
        assertThat(problem.getDetail()).isEqualTo("Die Eingaben sind unvollständig oder ungültig.");
        assertThat(Objects.requireNonNull(problem.getProperties()).get("fieldErrors"))
                .isEqualTo(Map.of("name", "ungültig", "start", "must be in future"));
    }
}
