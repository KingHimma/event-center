package eventcenter.scheduler.quartz;

import eventcenter.scheduler.EventTrigger;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import eventcenter.api.EventInfo;
import eventcenter.scheduler.FilterReceipt;

/**
 * 使用quartz的事件任务
 * @author JackyLIU
 *
 */
public final class QuartzEventJob implements Job {
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public QuartzEventJob(){
		
	}
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		EventInfo eventInfo = (EventInfo)context.getJobDetail().getJobDataMap().get(QuartzConstants.DATA_EVENT_INFO);		
		EventTrigger trigger = (EventTrigger)context.getJobDetail().getJobDataMap().get(QuartzConstants.DATA_EVENT_TRIGGER);
		if(null == eventInfo){
			logger.error("executed job failure, can't find eventInfo args");
			throw new JobExecutionException("lose eventInfo, can't fire schedule");
		}
		if(null == trigger){
			logger.error("executed job failure, can't find eventTrigger args");
			throw new JobExecutionException("lose eventTrigger, can't fire schedule");
		}
		
		// 如果有过滤器则执行过滤器
		if(trigger.getFilters() != null && trigger.getFilters().size() > 0){
			try{
				FilterReceipt receipt = trigger.filter(eventInfo);
				if(receipt == FilterReceipt.Rejected){
					if(logger.isDebugEnabled()){
						logger.debug("rejected schedule, eventInfo:" + eventInfo);
					}
					return ;
				}
				// 改变计划日程
				if(receipt == FilterReceipt.Changed){
					Trigger qt = ResolverRegister.getInstance().getResolvers().get(trigger.getClass().getName()).resolve(eventInfo, trigger);
					context.getScheduler().rescheduleJob(TriggerKey.triggerKey(eventInfo.getId()), qt);
					if(logger.isDebugEnabled()){
						logger.debug(new StringBuilder("reschedule event:").append(eventInfo).append(",trigger:").append(trigger));
					}
					return ;
				}
				if(receipt == FilterReceipt.Stop){
					context.getScheduler().deleteJob(JobKey.jobKey(eventInfo.getId()));
					if(logger.isDebugEnabled()){
						logger.debug(new StringBuilder("stop schedule event:").append(eventInfo).append(" success"));
					}
					return ;
				}
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
		
		QuartzEventContext.getInstance().getEventCenter().fireEvent(this, eventInfo, null);
		if(logger.isDebugEnabled()){
			logger.debug("fireEvent success, eventInfo:" + eventInfo);
		}
	}

}
