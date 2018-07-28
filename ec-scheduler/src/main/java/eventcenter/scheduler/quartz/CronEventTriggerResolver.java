package eventcenter.scheduler.quartz;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.UUID;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eventcenter.api.EventInfo;
import eventcenter.scheduler.CronEventTrigger;
import eventcenter.scheduler.EventTrigger;

/**
 * 将{@link CronEventTrigger}解析为{@link Trigger}
 * @author JackyLIU
 *
 */
public class CronEventTriggerResolver implements
        EventTriggerResolver {

	@Override
	public Trigger resolve(EventInfo eventInfo, EventTrigger eTrigger) {
		CronEventTrigger eventTrigger = (CronEventTrigger)eTrigger;
		if(eventTrigger.getCron() == null)
			throw new IllegalArgumentException("property of cron has to be set");
		
		TriggerBuilder<Trigger> builder = newTrigger().withIdentity(eventInfo.getId()==null?UUID.randomUUID().toString():eventInfo.getId());
		return builder.withSchedule(CronScheduleBuilder.cronSchedule(eventTrigger.getCron())).build();
	}

}
