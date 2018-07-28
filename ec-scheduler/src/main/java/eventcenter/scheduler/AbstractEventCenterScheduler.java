package eventcenter.scheduler;

import org.apache.log4j.Logger;

import eventcenter.api.EventCenter;

/**
 * 这个组合了事件中心的抽象类
 * @author JackyLIU
 *
 */
public abstract class AbstractEventCenterScheduler implements EventCenterScheduler {

	protected final EventCenter eventCenter;
	
	protected volatile boolean start;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public AbstractEventCenterScheduler(EventCenter eventCenter){
		this.eventCenter = eventCenter;
	}
	
	/**
	 * 启动事件调度中心
	 */
	public abstract void startup() throws Exception;
	
	/**
	 * 关闭事件调度中心
	 */
	public abstract void shutdown() throws Exception;

	public EventCenter getEventCenter() {
		return eventCenter;
	}

	public boolean isStart() {
		return start;
	}

}
