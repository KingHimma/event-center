package eventcenter.api.aggregator;

import eventcenter.api.EventInfo;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventInfo;
import eventcenter.api.EventSourceBase;

/**
 * 事件可聚合调用的接口，EventCenter继承此接口，那么具有事件将多个结束的监听器的返回结果，聚合到一个返回结果中，并返回出来
 * @author JackyLIU
 *
 */
public interface EventAggregatable {
	
	/**
	 * 获取聚合并发运行容器
	 * @return
	 */
	AggregatorContainer getAggregatorContainer();

	/**
	 * 直接触发调用监听器，这个方法可以应用在一个事件有多个监听器，需要同时执行多个监听器，那么可以使用这个方法
	 * @param target
	 * @param eventInfo
	 * @param handler 当一个监听器抛出异常时，需要实现此回调
	 * @return
	 */
	void directFireAggregateEvent(Object target, EventInfo eventInfo, ListenerExceptionHandler handler);

	/**
	 * 直接触发调用监听器，这个方法可以应用在一个事件有多个监听器，需要同时执行多个监听器，那么可以使用这个方法
	 * @param target
	 * @param eventSource
	 * @param handler
	 */
	void directFireAggregateEvent(Object target, EventSourceBase eventSource, ListenerExceptionHandler handler);

	/**
	 * 触发事件，将事件通知给对应的事件订阅者，然后将所有的事件订阅者消费后的返回数据，进行聚合，并返回出来。
	 * 也就是这个方法为多个事件消费者提供并行容器，并将所有相关的消费者的返回消息进行聚合，然后返回。
	 * @param target 触发事件的目标
	 * @param eventInfo 包含了事件名称
	 * @param aggregator 聚合器
	 * @return 通过聚合器返回数据
	 */
	<T> T fireAggregateEvent(Object target, EventInfo eventInfo, ResultAggregator<T> aggregator);
	
	/**
	 * 将一个事件源拆分成多个事件源，并放入到并行运行环境并发执行，并聚合结果返回出来，如果监听器有多个，则会抛出异常，只允许有一个监听器订阅此事件
	 * @param target
	 * @param eventInfo
	 * @param spliter
	 * @param aggregator
	 * @return
	 */
	<T> T fireAggregateEvent(Object target, EventInfo eventInfo, EventSpliter spliter, ResultAggregator<T> aggregator);
}
