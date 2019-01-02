package utils.exceptions;

public class ObjectInstantiationException extends RuntimeException {
    public ObjectInstantiationException() {
        super();
    }

    public ObjectInstantiationException(String message) {
        super(message);
    }
}
