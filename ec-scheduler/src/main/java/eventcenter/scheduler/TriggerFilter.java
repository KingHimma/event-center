package eventcenter.scheduler;

import eventcenter.api.EventInfo;

import java.io.Serializable;

/**
 * 对事件计划任务触发器设置过滤器，过滤器主要目的是在注册任务之前，做一层判断，如果过滤器返回false，那么这个触发器将不会注册到调度中心
 * @author JackyLIU
 *
 */
public interface TriggerFilter extends Serializable{

	/**
	 * 在注册到计划任务之前的一层过滤，如果直接返回 {@link FilterReceipt#Rejected}或者 {@link FilterReceipt#Stop}，那么此事件将不会加入到计划任务中
	 * @param eventInfo
	 * @param eventTrigger
	 * @return
	 */
	FilterReceipt preSchedule(EventInfo eventInfo, EventTrigger eventTrigger);
	
	/**
	 * 这个过滤器在计划任务触发Job之后，并在发送事件之前执行过滤，这里将会执行一次过滤，通过FilterReceipt控制事件是否需要发送，如果返回{@link FilterReceipt#Scheduled}
	 * 那么表示按照正常计划执行，那么将会触发事件，如果返回 {@link FilterReceipt#Rejected}此次任务将不会触发事件，那么依然按照计划任务等待执行下一次计划
	 * 那么计划任务将改变行程安排，不会触发事件，如果返回 {@link FilterReceipt#Stop}将停止这个计划任务
	 * @param eventInfo 事件信息
	 * @param eventTrigger 调度任务触发信息
	 * @return
	 */
	FilterReceipt filter(EventInfo eventInfo, EventTrigger eventTrigger);
}
