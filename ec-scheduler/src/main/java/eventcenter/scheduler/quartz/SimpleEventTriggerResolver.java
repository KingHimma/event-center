package eventcenter.scheduler.quartz;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eventcenter.api.EventInfo;
import eventcenter.scheduler.EventTrigger;
import eventcenter.scheduler.SimpleEventTrigger;
import eventcenter.scheduler.utils.DateHelper;

/**
 * 解析{@link SimpleEventTrigger}类型到{@link Trigger}类型
 * @author JackyLIU
 *
 */
public class SimpleEventTriggerResolver implements
        EventTriggerResolver {

	@Override
	public Trigger resolve(EventInfo eventInfo, EventTrigger eTrigger) {
		SimpleEventTrigger eventTrigger = (SimpleEventTrigger)eTrigger;
		checkArguments(eventTrigger);
		TriggerBuilder<Trigger> builder = newTrigger().withIdentity(eventInfo.getId()==null?UUID.randomUUID().toString():eventInfo.getId())
					.endAt(eventTrigger.getEndAt()).startAt(eventTrigger.getStartAt());
		if(eventTrigger.getDelay() != null){
			if(eventTrigger.getDelay() == 0L){
				builder.startNow();
			}else{
				builder.startAt(DateHelper.add(new Date(), eventTrigger.getDelay(), eventTrigger.getTimeUnit()));
			}
		}
		if(!checkNeedSchedule(eventTrigger))
			return builder.build();
		SimpleScheduleBuilder schBuilder = SimpleScheduleBuilder.simpleSchedule();
		if(eventTrigger.getInterval() != null){
			if(TimeUnit.SECONDS == eventTrigger.getTimeUnit()){
				schBuilder.withIntervalInSeconds(eventTrigger.getInterval().intValue());
			}else if(TimeUnit.MILLISECONDS == eventTrigger.getTimeUnit()){
				schBuilder.withIntervalInMilliseconds(eventTrigger.getInterval().intValue());
			}else if(TimeUnit.MINUTES == eventTrigger.getTimeUnit()){
				schBuilder.withIntervalInMinutes(eventTrigger.getInterval().intValue());
			}else if(TimeUnit.HOURS == eventTrigger.getTimeUnit()){
				schBuilder.withIntervalInHours(eventTrigger.getInterval().intValue());
			}
		}
		
		if(eventTrigger.getForeverRepeat() != null && eventTrigger.getForeverRepeat()){
			schBuilder.repeatForever();
		}else if(eventTrigger.getRepeatCount() != null){
			schBuilder.withRepeatCount(eventTrigger.getRepeatCount());
		}else{
			schBuilder.withRepeatCount(1);
		}
		builder.withSchedule(schBuilder);
		return builder.build();
	}
	
	protected boolean checkNeedSchedule(SimpleEventTrigger trigger){
		if(trigger.getForeverRepeat() != null && trigger.getForeverRepeat())
			return true;
		if(trigger.getRepeatCount() != null && trigger.getRepeatCount() > 1)
			return true;
		
		return false;	
	}
	
	protected void checkArguments(SimpleEventTrigger eventTrigger){
		if(eventTrigger.getTimeUnit() != TimeUnit.SECONDS && eventTrigger.getTimeUnit() != TimeUnit.MILLISECONDS
				&& eventTrigger.getTimeUnit() != TimeUnit.MINUTES && eventTrigger.getTimeUnit() != TimeUnit.HOURS)
			throw new IllegalArgumentException("SimpleEentTrigger only support for TimeUnit of MILLISECONDS,SECONDS,MINUTES,HOURS");
		
		if(eventTrigger.getForeverRepeat() != null && eventTrigger.getForeverRepeat() && eventTrigger.getInterval() == null)
			throw new IllegalArgumentException("If enable foreverRepeat, the property of interval has to be set");
		
		if(eventTrigger.getStartAt() != null && eventTrigger.getDelay() != null)
			throw new IllegalArgumentException("property of startAt and delay of one can be set");
		
		if(eventTrigger.getDelay() != null && eventTrigger.getDelay() < 0L)
			throw new IllegalArgumentException("delay value must be zero or more than zero");
		
		if(eventTrigger.getForeverRepeat() != null && eventTrigger.getForeverRepeat() && eventTrigger.getRepeatCount() != null)
			throw new IllegalArgumentException("property of foreverRepeat and repeatCount of one can be set");
	}

}
