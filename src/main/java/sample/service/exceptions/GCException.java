package sample.service.exceptions;

import sample.service.exceptions.errors.ServiceError;

public class GCException extends RuntimeException {
    private ServiceError serviceError;
    private Object[] values;

    public GCException(ServiceError serviceError, Object... values) {
        this.serviceError = serviceError;
        this.values = values;
    }

    public GCException(Throwable cause, ServiceError serviceError, Object[] values) {
        super(cause);
        this.serviceError = serviceError;
        this.values = values;
    }

    public ServiceError getServiceError() {
        return serviceError;
    }

    public Object[] getValues() {
        return values;
    }
}
