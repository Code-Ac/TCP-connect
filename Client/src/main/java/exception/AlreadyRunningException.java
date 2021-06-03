package exception;

public class AlreadyRunningException extends IllegalStateException {
    public AlreadyRunningException() {
        super();
    }

    public AlreadyRunningException(String s) {
        super(s);
    }

    public AlreadyRunningException(Throwable cause) {
        super(cause);
    }

    public AlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}
