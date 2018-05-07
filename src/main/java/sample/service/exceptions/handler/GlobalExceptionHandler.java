package sample.service.exceptions.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import sample.service.exceptions.ExceptionMessage;
import sample.service.exceptions.InvalidArgumentException;
import sample.service.exceptions.InvalidReturnValue;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidArgumentException.class)
    public ExceptionMessage invalidArgumentException(InvalidArgumentException ex) {
        log.error("InvalidArgumentException: {}", ex.getMessage(), ex);

        return ExceptionMessage.builder()
                .message(ex.getMessage())
                .errors(Arrays.stream(ex.getValues())
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .build();
    }

    @ExceptionHandler(InvalidReturnValue.class)
    public ExceptionMessage invalidReturnValue(InvalidReturnValue ex) {
        log.error("InvalidArgumentValue: {}", ex.getMessage(), ex);

        return ExceptionMessage.builder()
                .message(ex.getMessage())
                .errors(Arrays.stream(ex.getValues())
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .build();
    }

}
