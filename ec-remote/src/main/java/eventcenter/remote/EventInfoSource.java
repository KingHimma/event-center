package eventcenter.remote;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventInfo;

import java.io.Serializable;

/**
 * 封装了{@link EventInfo}和事件的result事件源
 * @author JackyLIU
 *
 */
public class EventInfoSource extends CommonEventSource implements Serializable{
	
	public EventInfoSource(Object source, EventInfo eventInfo, Object result) {
		super(source, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), result, eventInfo.getMdcValue());
		this.eventInfo = eventInfo;
	}

	private Target target;

	private final EventInfo eventInfo;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6633904153166341071L;

	public EventInfo getEventInfo() {
		return eventInfo;
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
