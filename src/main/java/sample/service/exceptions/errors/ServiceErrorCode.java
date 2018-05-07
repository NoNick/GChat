package sample.service.exceptions.errors;

public enum ServiceErrorCode implements ServiceError {
    NULL_ARGUMENT,
    INVALID_ARGUMENT,
    NULL_RETURN_VALUE;

    @Override
    public String getCode() {
        return null;
    }
}
