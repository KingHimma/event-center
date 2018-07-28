package eventcenter.scheduler.quartz;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;
import eventcenter.scheduler.*;
import eventcenter.scheduler.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class QuartzEventCenterScheduler extends AbstractEventCenterScheduler {

	protected Scheduler scheduler;
	
	public QuartzEventCenterScheduler(EventCenter eventCenter) {
		super(eventCenter);
	}

	@Override
	public ScheduleReceipt scheduleEvent(EventInfo eventInfo,
                                         EventTrigger eventTrigger) throws ECSchedulerException {
		EventTriggerResolver resolver = ResolverRegister.getInstance().getResolvers().get(eventTrigger.getClass().getName());
		if(null == resolver)
			throw new IllegalArgumentException("can't find resolver for type of " + eventTrigger.getClass().getName());

		ScheduleReceipt receipt = new ScheduleReceipt();
		FilterReceipt freceipt = eventTrigger.preSchedule(eventInfo);
		if(freceipt != FilterReceipt.Scheduled && freceipt != FilterReceipt.Changed){
			receipt.setSuccess(false);
			return receipt;
		}
		
		JobDetail job = buildJobDetail(eventInfo, eventTrigger);
		Trigger trigger = resolver.resolve(eventInfo, eventTrigger);
		try {
			receipt.setId(job.getKey().getName());
			receipt.setSuccess(true);
			scheduler.scheduleJob(job, trigger);
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("schedule job[").append(receipt.getId()).append("] success!"));
			}
			return receipt;
		} catch (SchedulerException e) {
			throw new ECSchedulerException(e);
		}
	}
	
	protected JobDetail buildJobDetail(EventInfo eventInfo, EventTrigger eventTrigger){
		JobDataMap data = new JobDataMap();
		data.put(QuartzConstants.DATA_EVENT_INFO, eventInfo);
		data.put(QuartzConstants.DATA_EVENT_TRIGGER, eventTrigger);
		return JobBuilder.newJob(QuartzEventJob.class)
				.usingJobData(data)
				.withIdentity(eventInfo.getId()==null?UUID.randomUUID().toString():eventInfo.getId())
				.build();
	}

	@PostConstruct
	@Override
	public void startup() throws Exception {
		if(start){
			logger.warn("scheduler had been startup.");
			return ;
		}
		if(null == eventCenter){
			throw new IllegalArgumentException("eventCenter has to be set");
		}
		StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
		scheduler = schedulerFactory.getScheduler();
		scheduler.start();
		QuartzEventContext.getInstance().setEventCenter(eventCenter);
		start = true;
	}

	@PreDestroy
	@Override
	public void shutdown() throws Exception{
		if(!start){
			logger.warn("scheduler haven't been startup.");
			return ;
		}
		scheduler.shutdown();
		start = false;
	}

	@Override
	public List<TriggerState> getTriggerStates() throws ECSchedulerException {
		try {
			Set<TriggerKey> keys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
			List<TriggerState> list = new ArrayList<TriggerState>();
			if(keys.size() == 0) {
				return list;
			}

			for(TriggerKey key : keys){
				Trigger trigger = scheduler.getTrigger(key);
				JobDetail job = scheduler.getJobDetail(JobKey.jobKey(key.getName()));
				if(null == trigger)
					continue;
				TriggerState state = new TriggerState();
				state.setEventInfo((EventInfo)job.getJobDataMap().get(QuartzConstants.DATA_EVENT_INFO));
				state.setId(key.getName());
				ScheduleState ss = new ScheduleState();
				ss.setStartTime(trigger.getStartTime());
				ss.setEndTime(trigger.getEndTime());
				ss.setNextFireTime(trigger.getNextFireTime());
				ss.setPreviousFireTime(trigger.getPreviousFireTime());
				state.setScheduleState(ss);
				list.add(state);
			}
			return list;
		} catch (SchedulerException e) {
			throw new ECSchedulerException(e);
		}
	}

	@Override
	public boolean killScheduler(String id) throws ECSchedulerException {
		try {
			return scheduler.deleteJob(JobKey.jobKey(id));
		} catch (SchedulerException e) {
			throw new ECSchedulerException(e);
		}
	}

}
