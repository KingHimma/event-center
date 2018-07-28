package eventcenter.scheduler;

import eventcenter.api.EventInfo;

import java.util.List;

/**
 * 事件调度中心接口
 * @author JackyLIU
 *
 */
public interface EventCenterScheduler {
	
	/**
	 * 获取任务的状态
	 * @return
	 */
	List<TriggerState> getTriggerStates() throws ECSchedulerException;

	/**
	 * 动态添加计划任务
	 * @param eventInfo
	 * @param trigger
	 * @return
	 */
	ScheduleReceipt scheduleEvent(EventInfo eventInfo, EventTrigger trigger) throws ECSchedulerException;
	
	/**
	 * 删掉某个计划任务，这个id是从 {@link #scheduleEvent(EventInfo, EventTrigger)}方法返回的编号
	 * @param id
	 * @return 如果返回true表示删除成功，如果返回false表示任务不存在
	 */
	boolean killScheduler(String id) throws ECSchedulerException;
}
