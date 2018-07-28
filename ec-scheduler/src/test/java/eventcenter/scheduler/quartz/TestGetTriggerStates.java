package eventcenter.scheduler.quartz;

import java.util.List;

import eventcenter.scheduler.CronEventTrigger;
import eventcenter.scheduler.ECSchedulerException;
import eventcenter.scheduler.ScheduleReceipt;
import eventcenter.scheduler.TriggerState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;

public class TestGetTriggerStates {
	private QuartzEventCenterScheduler scheduler;
	
	private EventCenter eventCenter;
	
	public TestGetTriggerStates(){
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception{
		eventCenter = Mockito.mock(EventCenter.class);
		scheduler = new QuartzEventCenterScheduler(eventCenter);
		
		scheduler.startup();
	}
	
	@After
	public void tearDown() throws Exception{
		scheduler.shutdown();
		Mockito.reset(eventCenter);
	}
	
	@Test
	public void test() throws ECSchedulerException, InterruptedException {
		CronEventTrigger trigger = new CronEventTrigger();
		trigger.setCron("0/2 * * * * ?");
		ScheduleReceipt receipt = scheduler.scheduleEvent(new EventInfo("test").setArgs(new Object[]{"Hello World"}), trigger);
		Assert.assertEquals(true, receipt.isSuccess());
		Assert.assertEquals(true, receipt.getId() != null);
		
		Thread.sleep(2000);
		List<TriggerState> triggerStates = scheduler.getTriggerStates();
		TriggerState state = null;
		for(TriggerState triggerState : triggerStates){
			if(triggerState.getId().equals(receipt.getId())){
				state = triggerState;
			}
		}
		
		Assert.assertEquals(true, state != null);
		Assert.assertNotNull(state.getEventInfo());
		Assert.assertNotNull(state.getScheduleState().getNextFireTime());
		Assert.assertNotNull(state.getScheduleState().getPreviousFireTime());
		
	}

}
