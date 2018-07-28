package eventcenter.scheduler.quartz;

import java.util.ArrayList;
import java.util.List;

import eventcenter.scheduler.*;
import eventcenter.scheduler.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;
import eventcenter.scheduler.TriggerFilter;

public class TestFilterSchedule {
	private QuartzEventCenterScheduler scheduler;
	
	private EventCenter eventCenter;
	
	public TestFilterSchedule(){
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
		List<TriggerFilter> filters = new ArrayList<TriggerFilter>();
		filters.add(new TriggerFilter(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 3560991472703478410L;

			@Override
			public FilterReceipt filter(EventInfo eventInfo, EventTrigger eventTrigger) {
				return FilterReceipt.Scheduled;
			}

			@Override
			public FilterReceipt preSchedule(EventInfo eventInfo,
					EventTrigger eventTrigger) {
				return FilterReceipt.Scheduled;
			}
		});
		trigger.setFilters(filters);
		ScheduleReceipt receipt = scheduler.scheduleEvent(new EventInfo("test").setArgs(new Object[]{"Hello World"}), trigger);
		Assert.assertEquals(true, receipt.isSuccess());		
		
		Thread.sleep(2100);
		Mockito.verify(eventCenter, Mockito.atLeastOnce()).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
		
	}

}
