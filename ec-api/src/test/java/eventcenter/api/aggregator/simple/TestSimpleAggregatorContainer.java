package eventcenter.api.aggregator.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.aggregator.*;
import eventcenter.api.annotation.ListenerBind;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class TestSimpleAggregatorContainer {

	private SimpleAggregatorContainer container;
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener1 listener1 = Mockito.spy(new AggregatorListener1());
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener2 listener2 = Mockito.spy(new AggregatorListener2());
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener3 listener3 = Mockito.spy(new AggregatorListener3());
	
	@Before
	public void setUp(){
		container = new SimpleAggregatorContainer();	
		Mockito.reset(listener1, listener2, listener3);
	}

	@After
	public void tearDown() throws Exception {
		container.close();
	}

	@Test
	public void test1() throws InterruptedException {
		AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World"}, null, null));
		
		ListenersConsumedResult results = container.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler(){
			@Override
			public Object handle(EventListener listener,
					EventSourceBase source, Exception e) {
				return null;
			}
		});
		
		Mockito.verify(listener1, Mockito.atLeastOnce()).onObserved(source);
		Mockito.verify(listener2, Mockito.atLeastOnce()).onObserved(source);
		
		Assert.assertEquals(results.getSource(), source);
		
		for(ListenerConsumedResult result : results.getResults()){
			Assert.assertEquals(true, result.getResult().toString().equals("Hello") || result.getResult().toString().equals("World"));
		}
	}
	
	@Test
	public void test2() throws InterruptedException, ExecutionException{
		int count = 100;
		List<AggregatorEventSource> sources = new ArrayList<AggregatorEventSource>(count);
		for(int i = 0;i < count;i++){
			sources.add(new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World", i}, null, null)));
		}
		
		ExecutorService tp = Executors.newFixedThreadPool(4);
		List<Callable<ListenersConsumedResult>> tasks = new ArrayList<Callable<ListenersConsumedResult>>();
		for(AggregatorEventSource source : sources){
			tasks.add(new Task(source));
		}
		
		System.out.println("添加任务，任务数：" + count);
		long start = System.currentTimeMillis();
		List<Future<ListenersConsumedResult>>  futures = tp.invokeAll(tasks);
		
		List<ListenersConsumedResult> results = new ArrayList<ListenersConsumedResult>(futures.size());
		for(Future<ListenersConsumedResult> future : futures){
			results.add(future.get());
		}
		System.out.println("结束批量调用，所花时间：" + (System.currentTimeMillis() - start) + " ms.");
		
		Mockito.verify(listener1, Mockito.atLeast(count)).onObserved(Mockito.any(EventSourceBase.class));
		Mockito.verify(listener2, Mockito.atLeast(count)).onObserved(Mockito.any(EventSourceBase.class));
		
		Assert.assertEquals(count, results.size());
		Integer prevNum = null;
		for(ListenersConsumedResult result : results){
			for(ListenerConsumedResult r : result.getResults()){
				Assert.assertEquals(true, r.getResult().toString().equals("Hello") || r.getResult().toString().equals("World"));
			}
			AggregatorEventSource src = (AggregatorEventSource)result.getSource();
			if(null != prevNum){
				Assert.assertEquals(false, prevNum.intValue() == src.getArg(2, Integer.class));
			}
			prevNum = src.getArg(2, Integer.class);
		}
	}
	
	/**
	 * 测试拆分聚合类型
	 * @throws InterruptedException 
	 */
	@Test
	public void testWithSplit() throws InterruptedException{
		EventSourceBase source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 1}, null, null));
		EventSourceBase source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 2}, null, null));
		EventSourceBase source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 3}, null, null));
		
		ListenersConsumedResult results = container.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler(){
			@Override
			public Object handle(EventListener listener,
					EventSourceBase source, Exception e) {
				return null;
			}
		});
		
		Assert.assertEquals(3, results.getResults().size());
		int total = 0;
		for(ListenerConsumedResult result : results.getResults()){
			Integer num = (Integer)result.getResult();
			total += num;
		}
		Assert.assertEquals(6, total);
		Mockito.verify(listener3, Mockito.atLeast(3)).onObserved(Mockito.any(EventSourceBase.class));
	}
	
	@ListenerBind("test1")
	class AggregatorListener1 implements AggregatorEventListener {

		@Override
		public void onObserved(EventSourceBase source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			
			// 将执行的结果放入到结果中
			evt.putResult(this, evt.getArg(0, String.class));
		}
		
	}
	
	@ListenerBind("test1")
	class AggregatorListener2 implements AggregatorEventListener {

		@Override
		public void onObserved(EventSourceBase source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			evt.putResult(this, evt.getArg(1, String.class));
		}
		
	}
	
	@ListenerBind("test2")
	class AggregatorListener3 implements AggregatorEventListener {
		
		@Override
		public void onObserved(EventSourceBase source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			evt.putResult(this, evt.getArg(2, Integer.class));
		}
		
	}
	
	class Task implements Callable<ListenersConsumedResult>{

		private final EventSourceBase source;
		
		public Task(EventSourceBase source){
			this.source = source;
		}
		
		@Override
		public ListenersConsumedResult call() throws Exception {
			return container.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler(){
				@Override
				public Object handle(EventListener listener,
						EventSourceBase source, Exception e) {
					return null;
				}
			});
		}
		
	}

}
