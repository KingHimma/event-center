package eventcenter.api;

/**
 * 
 * @author JackyLIU
 *
 */
public class InvalidAnnotationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public InvalidAnnotationException(final String message) {
        super(message);
    }
}
