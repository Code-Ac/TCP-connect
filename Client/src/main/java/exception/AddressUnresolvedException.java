package exception;

public class AddressUnresolvedException extends IllegalArgumentException {
    public AddressUnresolvedException() {
        super();
    }

    public AddressUnresolvedException(String s) {
        super(s);
    }

    public AddressUnresolvedException(Throwable cause) {
        super(cause);
    }

    public AddressUnresolvedException(String message, Throwable cause) {
        super(message, cause);
    }
}
