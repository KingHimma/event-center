package eventcenter.api.aggregator;

import java.util.HashMap;
import java.util.Map;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;

/**
 * 并发聚合时，封装的事件源
 * @author JackyLIU
 *
 */
public class AggregatorEventSource extends CommonEventSource {

	/**
	 * 原始事件源
	 */
	protected final EventSourceBase originalSource;
	
	/**
	 * 用于每个单独任务执行的结果
	 */
	private Map<Integer, Object> results;
	
	public AggregatorEventSource(EventSourceBase base, Object[] args, Object result, String mdcValue) {
		super(base.getSource(), base.getEventId(), base.getEventName(), args, result, mdcValue);
		this.originalSource = base;
		this.results = new HashMap<Integer, Object>();
	}
	
	public AggregatorEventSource(CommonEventSource base){
		this(base, base.getArgs(), base.getResult(), base.getMdcValue());
	}
	
	/**
	 * 放置某单个监听器消费的返回值
	 * @param listener
	 * @param result
	 */
	public void putResult(EventListener listener, Object result){
		results.put(listener.hashCode(), result);
	}
	
	public Object getResult(EventListener listener){
		return results.get(listener.hashCode());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1785059506589997689L;

	public EventSourceBase getOriginalSource() {
		return originalSource;
	}

}
