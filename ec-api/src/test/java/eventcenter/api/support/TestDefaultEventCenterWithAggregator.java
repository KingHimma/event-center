package eventcenter.api.support;

import eventcenter.api.*;
import eventcenter.api.EventListener;
import eventcenter.api.aggregator.*;
import eventcenter.api.aggregator.simple.AbstractSimpleEventSpliter;
import eventcenter.api.aggregator.support.ListAppendResultAggregator;
import eventcenter.api.aggregator.support.ReferenceResultAggregator;
import eventcenter.api.annotation.ListenerBind;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author JackyLIU
 *
 */
public class TestDefaultEventCenterWithAggregator {

	private DefaultEventCenter eventCenter;
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private eventcenter.api.EventListener listener1;
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private eventcenter.api.EventListener listener2;
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private eventcenter.api.EventListener listener3;

	private AtomicInteger count = new AtomicInteger(0);

	
	@Before
	public void setUp() throws Exception{
		EventCenterConfig config = new EventCenterConfig();
		CommonEventListenerConfig cc = new CommonEventListenerConfig();
		Map<String, List<eventcenter.api.EventListener>> listeners = new HashMap<String, List<eventcenter.api.EventListener>>();
		listener1 = Mockito.spy(new AggregatorListener1());
		listener2 = Mockito.spy(new AggregatorListener2());
		listener3 = Mockito.spy(new AggregatorListener3());
		Listener1 listener4 = new Listener1();
		Listener2 listener5 = new Listener2();
		listeners.put("test1", Arrays.asList(listener1, listener2));
		listeners.put("test2", Arrays.asList(listener3));
		listeners.put("test3", Arrays.asList(listener4, listener5));
		cc.setListeners(listeners);
		config.loadCommonEventListenerConfig(cc);
		eventCenter = Mockito.spy(new DefaultEventCenter());
		eventCenter.setEcConfig(config);

		count.set(0);
		eventCenter.startup();
	}
	
	@After
	public void tearDown() throws Exception{
		eventCenter.shutdown();
	}
	
	@Test
	public void test() {
		Integer count = eventCenter.fireAggregateEvent(this, new EventInfo("test1").setArgs(new Object[]{1,2}), new ResultAggregator<Integer>() {

			@Override
			public Integer aggregate(ListenersConsumedResult result) {
				int count = 0;
				for(ListenerConsumedResult r : result.getResults()){
					count += (Integer)r.getResult();
				}
				return count;
			}

			@Override
			public Object exceptionHandler(eventcenter.api.EventListener listener,
										   CommonEventSource source, Exception e) {
				return null;
			}
			
		});
		
		Mockito.verify(listener1, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		Mockito.verify(listener2, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		Assert.assertEquals(3, count.intValue());
	}
	
	@Test
	public void testWithReferenceAggregator(){
		Integer count = eventCenter.fireAggregateEvent(this, new EventInfo("test1").setArgs(new Object[]{1,2}), new ReferenceResultAggregator<Integer>(1));
		Assert.assertEquals(2, count.intValue());
		Mockito.verify(listener1, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		Mockito.verify(listener2, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
	}
	
	@Test
	public void test4Split(){
		List<Integer> list = eventCenter.fireAggregateEvent(this, new EventInfo("test2").setArgs(new Object[]{Arrays.asList(1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10)}), new AbstractSimpleEventSpliter() {
			@Override
			protected List<Object> splitArgs(Object[] args) {
				@SuppressWarnings("unchecked")
				List<Integer> _args = (List<Integer>)args[0];
				final int mode = 5;
				List<Object> list = new ArrayList<Object>();
				Object[] subArgs = null;
				for(int i = 0;i < _args.size();i++){
					if(i%mode == 0){
						if(null != subArgs){
							list.add(subArgs);
						}
						subArgs = new Object[mode];
					}
					subArgs[i%mode] = _args.get(i);
				}
				if(null != subArgs && subArgs.length != 0){
					list.add(subArgs);
				}
				return list;
			}
		}, new ListAppendResultAggregator<Integer>());
		
		Assert.assertEquals(30, list.size());
		Assert.assertEquals(2, list.get(0).intValue());
		Assert.assertEquals(3, list.get(1).intValue());
		Assert.assertEquals(4, list.get(2).intValue());
		Assert.assertEquals(5, list.get(3).intValue());
		Assert.assertEquals(6, list.get(4).intValue());
		Assert.assertEquals(7, list.get(5).intValue());
		Assert.assertEquals(8, list.get(6).intValue());
		Assert.assertEquals(9, list.get(7).intValue());
		Assert.assertEquals(10, list.get(8).intValue());
		Assert.assertEquals(11, list.get(9).intValue());
		Assert.assertEquals(2, list.get(10).intValue());
		Assert.assertEquals(3, list.get(11).intValue());
		Assert.assertEquals(4, list.get(12).intValue());
		Assert.assertEquals(5, list.get(13).intValue());
		Assert.assertEquals(6, list.get(14).intValue());
		Assert.assertEquals(7, list.get(15).intValue());
		Assert.assertEquals(8, list.get(16).intValue());
		Assert.assertEquals(9, list.get(17).intValue());
		Assert.assertEquals(10, list.get(18).intValue());
		Assert.assertEquals(11, list.get(19).intValue());
		Assert.assertEquals(2, list.get(20).intValue());
		Assert.assertEquals(3, list.get(21).intValue());
		Assert.assertEquals(4, list.get(22).intValue());
		Assert.assertEquals(5, list.get(23).intValue());
		Assert.assertEquals(6, list.get(24).intValue());
		Assert.assertEquals(7, list.get(25).intValue());
		Assert.assertEquals(8, list.get(26).intValue());
		Assert.assertEquals(9, list.get(27).intValue());
		Assert.assertEquals(10, list.get(28).intValue());
		Assert.assertEquals(11, list.get(29).intValue());
	}

	@Test
	public void testDirectFireAggregateEvent1(){
		long start = System.currentTimeMillis();
		eventCenter.directFireAggregateEvent(this, new EventInfo("test3").setArgs(new Object[]{"1"}), null);
		long took = System.currentTimeMillis() - start;
		Assert.assertTrue(took < 1100);
		Assert.assertTrue(took > 1000);
		Assert.assertEquals(2, count.get());
	}

	@ListenerBind("test1")
	class AggregatorListener1 implements AggregatorEventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			
			// 将执行的结果放入到结果中
			evt.putResult(this, evt.getArg(0, Integer.class));
		}
		
	}
	
	@ListenerBind("test1")
	class AggregatorListener2 implements AggregatorEventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			evt.putResult(this, evt.getArg(1, Integer.class));
		}
		
	}

	@ListenerBind(("test3"))
	class Listener1 implements eventcenter.api.EventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			count.incrementAndGet();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@ListenerBind(("test3"))
	class Listener2 implements EventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			count.incrementAndGet();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@ListenerBind("test2")
	class AggregatorListener3 implements AggregatorEventListener {
		
		@Override
		public void onObserved(CommonEventSource source) {
			AggregatorEventSource evt = (AggregatorEventSource)source;
			Object[] args = evt.getArg(0, Object[].class);
			List<Integer> nArgs = new ArrayList<Integer>(args.length);
			for(Object arg : args){
				Integer v = (Integer)arg;
				nArgs.add(v + 1);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			evt.putResult(this, nArgs);
		}
		
	}

}
