package sample.utils;

public class SimpleValidator {

    public static void validateObject(Object object, String msg) {
        if (object == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}
