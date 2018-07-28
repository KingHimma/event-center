package eventcenter.api.aggregator;

import eventcenter.api.CommonEventSource;

import java.util.List;

/**
 * 事件消费的运行聚合容器
 * @author JackyLIU
 *
 */
public interface AggregatorContainer {

	/**
	 * 并发运行各个监听器，参数source必须是继承于AggregatorEventSource
	 * @param listeners
	 * @return
	 */
	ListenersConsumedResult executeListeners(List<AggregatorEventListener> listeners, CommonEventSource source, ListenerExceptionHandler handler) throws InterruptedException;
	
	/**
	 * 并发运行一个监听器的多个事件源，参数sources必须是继承于AggregatorEventSource
	 * @param listener
	 * @param sources
	 * @param handler
	 * @return
	 * @throws InterruptedException
	 */
	ListenersConsumedResult executeListenerSources(AggregatorEventListener listener, List<CommonEventSource> sources, ListenerExceptionHandler handler) throws InterruptedException;
}
