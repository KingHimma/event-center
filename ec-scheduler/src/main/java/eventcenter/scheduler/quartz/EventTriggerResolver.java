package eventcenter.scheduler.quartz;

import org.quartz.Trigger;

import eventcenter.api.EventInfo;
import eventcenter.scheduler.EventTrigger;

/**
 * 事件任务触发器解析器
 * @author JackyLIU
 *
 */
public interface EventTriggerResolver {

	Trigger resolve(EventInfo eventInfo, EventTrigger eventTrigger);
}
