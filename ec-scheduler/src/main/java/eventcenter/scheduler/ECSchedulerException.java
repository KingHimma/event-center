package eventcenter.scheduler;

/**
 * 事件调度中心异常
 * @author JackyLIU
 *
 */
public class ECSchedulerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -293461211454351912L;


	public ECSchedulerException(Exception e){
		super(e);
	}
	
	public ECSchedulerException(String msg){
		super(msg);
	}
}
