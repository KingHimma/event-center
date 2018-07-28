package eventcenter.scheduler.quartz;

import eventcenter.api.EventCenter;

/**
 * 使用Quartz的事件调度中心的上下文
 * @author JackyLIU
 *
 */
public class QuartzEventContext {

	private static QuartzEventContext self;
	
	private EventCenter eventCenter;
	
	public static synchronized QuartzEventContext getInstance(){
		if(null == self){
			self = new QuartzEventContext();
		}
		return self;
	}

	public EventCenter getEventCenter() {
		return eventCenter;
	}

	void setEventCenter(EventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}
}
