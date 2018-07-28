package eventcenter.scheduler.quartz;

import java.util.concurrent.TimeUnit;

import eventcenter.scheduler.ECSchedulerException;
import eventcenter.scheduler.ScheduleReceipt;
import eventcenter.scheduler.SimpleEventTrigger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;

public class TestKillScheduler {
private QuartzEventCenterScheduler scheduler;
	
	private EventCenter eventCenter;
	
	public TestKillScheduler(){
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
		SimpleEventTrigger trigger = new SimpleEventTrigger();
		trigger.setDelay(2L);
		trigger.setTimeUnit(TimeUnit.SECONDS);
		ScheduleReceipt receipt = scheduler.scheduleEvent(new EventInfo("test").setArgs(new Object[]{"Hello World"}), trigger);
		Assert.assertEquals(true, receipt.isSuccess());
		Assert.assertEquals(true, receipt.getId() != null);
		
		Thread.sleep(1800);
		
		Assert.assertEquals(true, scheduler.killScheduler(receipt.getId()));
		
		Mockito.verify(eventCenter, Mockito.atLeast(0)).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
	

		Thread.sleep(500);
		Mockito.verify(eventCenter, Mockito.atLeast(0)).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
		
	}

}
