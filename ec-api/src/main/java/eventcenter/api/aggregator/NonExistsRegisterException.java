package eventcenter.api.aggregator;

/**
 * 无法找到事件注册者异常
 * @author JackyLIU
 *
 */
public class NonExistsRegisterException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9184687135148429210L;

	public NonExistsRegisterException(String msg){
		super(msg);
	}
}
