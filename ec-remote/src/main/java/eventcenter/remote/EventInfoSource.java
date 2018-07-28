package eventcenter.remote;

import java.io.Serializable;

import eventcenter.api.EventInfo;
import eventcenter.api.EventSourceBase;

/**
 * 封装了{@link EventInfo}和事件的result事件源
 * @author JackyLIU
 *
 */
public class EventInfoSource extends EventSourceBase implements Serializable{
	
	public EventInfoSource(Object source, String eventId, String eventName, String mdcValue) {
		super(source, eventId, eventName, mdcValue);
	}
	
	public EventInfoSource(){
		super("", null ,null, null);
	}

	private Target target;

	private EventInfo eventInfo;
	
	private Object result;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6633904153166341071L;

	public EventInfo getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(EventInfo eventInfo) {
		this.eventInfo = eventInfo;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(null != eventId && null != eventName){
			sb.append("{id:").append(eventId).append(",name:").append(eventName);
		}else if(null != eventInfo){
			sb.append("{id:").append(eventInfo.getId()).append(",name:").append(eventInfo.getName());
		}else{
			sb.append("{}");
		}
		return sb.toString();
	}

}
