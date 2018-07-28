package eventcenter.api.aggregator;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;

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
	Object handle(EventListener listener, EventSourceBase source, Exception e);
}
