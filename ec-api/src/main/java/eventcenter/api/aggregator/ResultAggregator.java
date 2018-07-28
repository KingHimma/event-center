package eventcenter.api.aggregator;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;

/**
 * 事件消费的结果集聚合器
 * @author JackyLIU
 *
 */
public interface ResultAggregator<T> {

	/**
	 * 将事件返回的多个结果集聚合到一个结果集中处理
	 * @param result
	 * @return
	 */
	T aggregate(ListenersConsumedResult result);
	
	/**
	 * 当某个监听器处理时出现异常时，将会调用此接口处理
	 * @param listener
	 * @param source
	 * @param e
	 * @return
	 */
	Object exceptionHandler(EventListener listener, EventSourceBase source, Exception e);
}
