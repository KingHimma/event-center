package eventcenter.api.aggregator.support;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.aggregator.ListenerConsumedResult;
import eventcenter.api.aggregator.ListenersConsumedResult;
import eventcenter.api.aggregator.ResultAggregator;

/**
 * 通用的解决策略，事件中的参数分配到每个监听器后，监听器处理某个参数的对象或者集合，处理之后，只需要返回监听器的事件参数即可。
 * 默认获取事件参数的第一个
 * <p> 如果事件使用的是非{@link CommonEventSource}类型，那么默认取其中一个{@link ListenerConsumedResult}返回的结果
 * @author JackyLIU
 *
 * @param <T>
 */
public class ReferenceResultAggregator<T> implements ResultAggregator<T> {

	private int returnArgIndex = 0;
	
	public ReferenceResultAggregator(){
		
	}
	
	/**
	 * 监听器所返回的事件参数的下标
	 * @param returnArgIndex
	 */
	public ReferenceResultAggregator(int returnArgIndex){
		this.returnArgIndex = returnArgIndex;
	}
	
	/**
	 * 直接返回result中的集合
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T aggregate(ListenersConsumedResult result) {
		return (T)((CommonEventSource)result.getSource()).getArgs()[returnArgIndex];
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object exceptionHandler(EventListener listener,
			CommonEventSource source, Exception e) {
		if(source instanceof CommonEventSource){
			return (T)(source).getArgs()[returnArgIndex];
		}
		return null;
	}

}
