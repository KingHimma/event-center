package eventcenter.scheduler;

import java.io.Serializable;

import eventcenter.api.EventInfo;

/**
 * 查询计划任务返回的触发器信息
 * @author JackyLIU
 *
 */
public class TriggerState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6998231356376979164L;

	/**
	 * 添加到计划任务的Job编号,也就是scheduleEvent返回的编号
	 */
	private String id;
	
	/**
	 * 事件源信息
	 */
	private EventInfo eventInfo;
	
	/**
	 * 计划任务的状态信息，包括触发时间，下次触发时间等等
	 */
	private ScheduleState scheduleState;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EventInfo getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(EventInfo eventInfo) {
		this.eventInfo = eventInfo;
	}

	public ScheduleState getScheduleState() {
		return scheduleState;
	}

	public void setScheduleState(ScheduleState scheduleState) {
		this.scheduleState = scheduleState;
	}
}
