package eventcenter.api.async;

/**
 * 队列异常
 * @author JackyLIU
 *
 */
public class QueueException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7406339319148230657L;

	public QueueException(String msg){
		super(msg);
	}
	
	public QueueException(Exception e){
		super(e);
	}
}
