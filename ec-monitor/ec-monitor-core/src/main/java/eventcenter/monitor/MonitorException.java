package eventcenter.monitor;

/**
 * Created by liumingjian on 16/2/15.
 */
public class MonitorException extends RuntimeException {

    public MonitorException(String message) {
        super(message);
    }

    public MonitorException(Throwable cause) {
        super(cause);
    }
}
