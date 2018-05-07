package sample.utils.validation;

import lombok.extern.slf4j.Slf4j;
import sample.service.exceptions.InvalidArgumentException;
import sample.service.exceptions.errors.ServiceErrorCode;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class GCValidator {

    public static void validateObject(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null!");
        }
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        if (violations.size() > 0) {
            List<String> errors = new ArrayList<>();

            for (ConstraintViolation<Object> violation : violations) {
                String e = violation.getPropertyPath() + " " + violation.getMessage();
                log.error(e);
                errors.add(e);
            }
            throw new InvalidArgumentException(ServiceErrorCode.INVALID_ARGUMENT, errors.toArray());
        }
    }
}
