package sample.service.exceptions;

import sample.service.exceptions.errors.ServiceError;

public class InvalidReturnValue extends GCException {

    public InvalidReturnValue(ServiceError serviceError, Object... values) {
        super(serviceError, values);
    }

}
