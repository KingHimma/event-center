package eventcenter.api;

/**
 * 事件监听器
 * @author JackyLIU
 *
 */
public interface EventListener {

	/**
	 * 当监控中心，监控到事件之后，将会调用此方法
	 * @param source
	 */
	void onObserved(EventSourceBase source);
	
}
