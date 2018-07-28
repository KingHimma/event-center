package eventcenter.monitor;

/**
 * 有关{@link InfoStorage}操作时抛出的异常
 * Created by liumingjian on 16/2/15.
 */
public class MonitorStorageException extends MonitorException {

    public MonitorStorageException(String message) {
        super(message);
    }

    public MonitorStorageException(Throwable cause) {
        super(cause);
    }
}
