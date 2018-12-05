package utils;

public class AppException extends RuntimeException {
    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
    }
}
