package eventcenter.api.aggregator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eventcenter.api.EventSourceBase;
import eventcenter.api.EventSourceBase;

/**
 * 监听器消费后返回的结果集
 * @author JackyLIU
 *
 */
public class ListenersConsumedResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7102881861037182047L;

	/**
	 * 事件名称
	 */
	private String eventName;
	
	/**
	 * 总共所消耗的时间
	 */
	private long took;
	
	/**
	 * 每个监听器消费后的结果集
	 */
	private List<ListenerConsumedResult> results;
	
	/**
	 * 事件源
	 */
	private EventSourceBase source;

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public long getTook() {
		return took;
	}

	public void setTook(long took) {
		this.took = took;
	}

	public List<ListenerConsumedResult> getResults() {
		if(null == results)
			results = new ArrayList<ListenerConsumedResult>();
		return results;
	}

	public void setResults(List<ListenerConsumedResult> results) {
		this.results = results;
	}

	public EventSourceBase getSource() {
		return source;
	}

	public void setSource(EventSourceBase source) {
		this.source = source;
		this.eventName = source.getEventName();
	}
}
