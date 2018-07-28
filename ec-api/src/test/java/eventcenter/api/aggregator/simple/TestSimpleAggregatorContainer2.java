package eventcenter.api.aggregator.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.aggregator.*;
import eventcenter.api.annotation.ListenerBind;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

public class TestSimpleAggregatorContainer2 {

	private SimpleAggregatorContainer container;

	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener1 listener1;

	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener2 listener2;

	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private AggregatorListener3 listener3;

	@Before
	public void setUp(){
		container = new SimpleAggregatorContainer();
	}

	@After
	public void tearDown() throws Exception {
		container.close();
	}

	@Test
	public void test1() throws InterruptedException {
		final Long delay = 5000L;
		listener1 = new AggregatorListener1(delay);
		listener2 = new AggregatorListener2(delay);
		long start = System.currentTimeMillis();
		AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World"}, null, null));

		ListenersConsumedResult results = container.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler(){
			@Override
			public Object handle(EventListener listener,
					CommonEventSource source, Exception e) {
				return null;
			}
		});
		long took = System.currentTimeMillis() - start;
		Assert.assertEquals(true, took <= delay + 50);
		Assert.assertEquals(results.getSource(), source);

		for(ListenerConsumedResult result : results.getResults()){
			Assert.assertEquals(true, result.getResult().toString().equals("Hello") || result.getResult().toString().equals("World"));
		}
	}

	@Test(expected = RejectedExecutionException.class)
	public void test2() throws InterruptedException {
		container = new SimpleAggregatorContainer(1,1);
		final Long delay = 2000L;
		listener1 = new AggregatorListener1(delay);
		listener2 = new AggregatorListener2(delay);
		AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World"}, null, null));

		container.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler(){
			@Override
			public Object handle(EventListener listener,
								 CommonEventSource source, Exception e) {
				return null;
			}
		});
	}

	/**
	 * 测试拆分聚合类型
	 * @throws InterruptedException
	 */
	@Test
	public void testWithSplit() throws InterruptedException{
		final Long delay = 5000L;
		listener3 = new AggregatorListener3(delay);
		CommonEventSource source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 1}, null, null));
		CommonEventSource source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 2}, null, null));
		CommonEventSource source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 3}, null, null));

		final long start = System.currentTimeMillis();
		ListenersConsumedResult results = container.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler() {
			@Override
			public Object handle(EventListener listener,
								 CommonEventSource source, Exception e) {
				return null;
			}
		});

		long took = System.currentTimeMillis() - start;
		Assert.assertTrue(took <= delay + 50);
		Assert.assertEquals(3, results.getResults().size());
		int total = 0;
		for(ListenerConsumedResult result : results.getResults()){
			Integer num = (Integer)result.getResult();
			total += num;
		}
		Assert.assertEquals(6, total);
	}

	@ListenerBind("test1")
	class AggregatorListener1 implements AggregatorEventListener {

		final Long delay;

		public AggregatorListener1(Long delay){
			if(null == delay){
				this.delay = 0L;
			}else{
				this.delay = delay;
			}
		}

		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;

			if(this.delay > 0L){
				try {
					Thread.sleep(this.delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// 将执行的结果放入到结果中
			evt.putResult(this, evt.getArg(0, String.class));
		}

	}

	@ListenerBind("test1")
	class AggregatorListener2 implements AggregatorEventListener {

		final Long delay;

		public AggregatorListener2(Long delay){
			if(null == delay){
				this.delay = 0L;
			}else{
				this.delay = delay;
			}
		}

		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			if(this.delay > 0L){
				try {
					Thread.sleep(this.delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			evt.putResult(this, evt.getArg(1, String.class));
		}

	}

	@ListenerBind("test2")
	class AggregatorListener3 implements AggregatorEventListener {

		final Long delay;

		public AggregatorListener3(Long delay){
			if(null == delay){
				this.delay = 0L;
			}else{
				this.delay = delay;
			}
		}

		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			if(this.delay > 0L){
				try {
					Thread.sleep(this.delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			evt.putResult(this, evt.getArg(2, Integer.class));
		}

	}

}
