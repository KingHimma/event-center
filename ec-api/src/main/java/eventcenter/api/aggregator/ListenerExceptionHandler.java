package eventcenter.api.aggregator;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;

/**
 * 聚合运行时，监听器异常处理回调
 * @author JackyLIU
 *
 */
public interface ListenerExceptionHandler {

	/**
	 * 异常处理方法
	 * @param listener
	 * @param source
	 * @param e
	 * @return
	 */
	Object handle(EventListener listener, CommonEventSource source, Exception e);
}
