package sample.service.exceptions;

import sample.service.exceptions.errors.ServiceError;

public class InvalidArgumentException extends GCException {

    public InvalidArgumentException(ServiceError serviceError, Object... values) {
        super(serviceError, values);
    }
}
