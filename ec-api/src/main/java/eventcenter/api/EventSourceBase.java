/**
 * @date 2014年4月17日
 * @author huangjie
 */
package eventcenter.api;

import java.io.Serializable;
import java.util.EventObject;


/**
 * 事件存储的基础类
 * @author JackyLIU
 */
public abstract class EventSourceBase extends EventObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 374607712679441042L;
	
	protected final String eventName;
	
	protected final String eventId;

	/**
	 * 如果在config中开启了mdcField,那么将会把这个mdc的值设置到事件源中
	 */
	protected String mdcValue;
	
	/**
	 * 同步消费事件时，监听器返回的结果对象，用于同步事件
	 */
	protected Object syncResult;

	/**
	 * 如果source是实现了序列化的接口，那么将会把这个存储在这个字段中
	 */
	private Object sourceInfo;

	/**
	 * 事件消费状态，默认事件消费成功，容器会自动设置为成功，如果消费失败，需要监听器内部捕获异常，并设置状态，目前TIMEOUT和FAILTURE两个状态
	 * 会产生重试的操作
	 */
	private EventConsumedStatus status;

	private String sourceClassName;

	public EventSourceBase(Object source, String eventId, String eventName, String mdcValue) {
		super(source);
		this.timestamp = System.currentTimeMillis();
		this.eventName = eventName;
		this.eventId = eventId;
		this.mdcValue = mdcValue;
		if(source instanceof Serializable){
			sourceInfo = source;
		}
	}
	
	/**
	 * 时间戳
	 */
	protected long timestamp;
	

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEventName() {
		return eventName;
	}
	
	public String getEventId() {
		return eventId;
	}

	public Object getSyncResult() {
		return syncResult;
	}

	public String getMdcValue() {
		return mdcValue;
	}

	public void setMdcValue(String mdcValue) {
		this.mdcValue = mdcValue;
	}

	public void setSyncResult(Object syncResult) {
		this.syncResult = syncResult;
	}

	public Object getSourceInfo() {
		return sourceInfo;
	}

	public void setSourceInfo(Object sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	public EventConsumedStatus getStatus() {
		return status;
	}

	public void setStatus(EventConsumedStatus status) {
		this.status = status;
	}

	public String getSourceClassName() {
		return sourceClassName;
	}

	public void setSourceClassName(String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{source:").append(source)
		.append(",eventId:").append(eventId)
		.append(",eventName:").append(eventName);
		if(null != mdcValue){
			sb.append(",mdcValue:").append(mdcValue);
		}
		sb.append("}");
		return sb.toString();
	}
}
