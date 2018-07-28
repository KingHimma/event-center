package eventcenter.api;

/**
 * 注册事件，注册者携带一到多个事件
 * @author JackyLIU
 *
 */
public interface EventRegister {
	
	/**
	 * 创建事件源，如果需要result，则事件发出的顺序是在监控点之后发出
	 * @param args
	 * @param result
	 * @return
	 */
	CommonEventSource createEventSource(Object source, String id, String eventName, Object[] args, Object result, String mdcValue);
	
	/**
	 * 创建事件监听器
	 * @return
	 */
	EventListener[] getEventListeners();
}
