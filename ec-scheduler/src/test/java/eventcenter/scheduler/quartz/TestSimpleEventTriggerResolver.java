package eventcenter.scheduler.quartz;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import eventcenter.scheduler.SimpleEventTrigger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Trigger;

import eventcenter.api.EventInfo;

public class TestSimpleEventTriggerResolver {

	private SimpleEventTriggerResolver resolver;
	
	private EventInfo eventInfo;
	
	@Before
	public void setUp(){
		resolver = new SimpleEventTriggerResolver();
		eventInfo = new EventInfo("sample");
	}
	
	@Test
	public void testStartAt() {
		SimpleEventTrigger et = new SimpleEventTrigger();
		Date date = new Date();
		et.setStartAt(date);
		Trigger trigger = resolver.resolve(eventInfo, et);
		Assert.assertEquals(date, trigger.getStartTime());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArguments1(){
		SimpleEventTrigger et = new SimpleEventTrigger();
		Date date = new Date();
		et.setStartAt(date);
		et.setDelay(1L);
		resolver.resolve(eventInfo, et);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArguments2(){
		SimpleEventTrigger et = new SimpleEventTrigger();
		et.setDelay(1L);
		et.setTimeUnit(TimeUnit.DAYS);
		resolver.resolve(eventInfo, et);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArguments3(){
		SimpleEventTrigger et = new SimpleEventTrigger();
		et.setDelay(1L);
		et.setTimeUnit(TimeUnit.MINUTES);
		et.setRepeatCount(4);
		et.setForeverRepeat(true);
		resolver.resolve(eventInfo, et);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArguments4(){
		SimpleEventTrigger et = new SimpleEventTrigger();
		et.setDelay(-1L);
		et.setTimeUnit(TimeUnit.MINUTES);
		resolver.resolve(eventInfo, et);
	}

}
