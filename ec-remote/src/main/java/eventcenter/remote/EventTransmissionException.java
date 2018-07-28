package eventcenter.remote;

/**
 * Created by liumingjian on 16/7/30.
 */
public class EventTransmissionException extends RuntimeException {
    private static final long serialVersionUID = 7549108635092161457L;

    public EventTransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTransmissionException(String message) {
        super(message);
    }
}
