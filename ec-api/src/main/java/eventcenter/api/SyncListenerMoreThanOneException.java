package eventcenter.api;

/**
 * 同一个事件名称的监听器如果使用同步方式调用（async=false），并且订阅的监听器有多个，则会抛出此异常
 * @author JackyLIU
 *
 */
public class SyncListenerMoreThanOneException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5109810680669507646L;
	
	public SyncListenerMoreThanOneException(){}
	
	public SyncListenerMoreThanOneException(String msg){
		super(msg);
	}

}
