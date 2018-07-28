package eventcenter.scheduler.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import eventcenter.scheduler.ECSchedulerException;
import eventcenter.scheduler.ScheduleReceipt;
import eventcenter.scheduler.SimpleEventTrigger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;

public class TestSimpleScheduler {

	private QuartzEventCenterScheduler scheduler;
	
	private EventCenter eventCenter;
	
	public TestSimpleScheduler(){
		//org.apache.log4j.BasicConfigurator.configure();
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
		Mockito.verify(eventCenter, Mockito.atLeast(0)).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
		
		Thread.sleep(500);
		Mockito.verify(eventCenter, Mockito.atLeastOnce()).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
		
		Thread.sleep(1800);
		Mockito.verify(eventCenter, Mockito.atLeastOnce()).fireEvent(Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
	}
	
	/**
	 * 压力测试
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	@Test
	public void test4Load() throws InterruptedException, ExecutionException{
		final int threadCount = 10;
		final int taskCount = 5000;
		final AtomicLong counter = new AtomicLong(0);
		Mockito.when(eventCenter.fireEvent(Mockito.anyObject(), Mockito.any(EventInfo.class), Mockito.anyObject())).then(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				counter.incrementAndGet();
				return null;
			}
		});
		
		
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		for(int i = 0;i < taskCount;i++){
			final int count = i + 1;
			tasks.add(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					SimpleEventTrigger trigger = new SimpleEventTrigger();
					trigger.setDelay(2L);
					trigger.setTimeUnit(TimeUnit.SECONDS);
					scheduler.scheduleEvent(new EventInfo("test").setArgs(new Object[]{count}), trigger);
					return null;
				}
			});
		}
		
		System.out.println("开始添加任务：" + taskCount);
		long start = System.currentTimeMillis();
		List<Future<Void>> futures = threadPool.invokeAll(tasks);
		for(Future<Void> future : futures){
			future.get();
		}
		System.out.println("添加任务成功, took:" + (System.currentTimeMillis() - start) + " ms.");
		
		Thread.sleep(10000);
		Assert.assertEquals(taskCount, counter.get());
	}

}
