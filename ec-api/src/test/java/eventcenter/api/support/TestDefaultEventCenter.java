package eventcenter.api.support;

import eventcenter.api.*;
import eventcenter.api.annotation.ListenerBind;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDefaultEventCenter {

	DefaultEventCenter eventCenter;
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private EventListener listener1 = Mockito.spy(new Listener1());
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private EventListener listener2 = Mockito.spy(new Listener2());
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private EventListener listener3 = Mockito.spy(new Listener3());
	
	/**
	 * 在测试之前就创建好，保持单例模式
	 */
	private EventListener listener4 = Mockito.spy(new Listener4());
	
	private EventListener listener5 = new Listener5();
	private EventListener listener6 = new Listener6();
	
	private final ListenerRef listenerRef = new ListenerRef();
	
	private long listener2Sleep = 1000;
	
	@Before
	public void setUp() throws Exception{
		EventCenterConfig config = new EventCenterConfig();
		CommonEventListenerConfig cc = new CommonEventListenerConfig();
		Map<String, List<EventListener>> listeners = new HashMap<String, List<EventListener>>();
		listeners.put("test1", Arrays.asList(listener1, listener2));
		listeners.put("test2", Arrays.asList(listener3));
		listeners.put("test3", Arrays.asList(listener4));
		listeners.put("test4", Arrays.asList(listener5, listener6));
		cc.setListeners(listeners);
		config.loadCommonEventListenerConfig(cc);
		eventCenter = Mockito.spy(new DefaultEventCenter());
		eventCenter.setEcConfig(config);
		
		eventCenter.startup();
		
		Mockito.reset(listener1, listener2, listener3, listener4);
		listenerRef.listener1 = null;
		listenerRef.listener1Result = null;
		listenerRef.listener2 = null;
		listenerRef.listener3 = null;
	}
	
	@After
	public void tearDown() throws Exception{
		eventCenter.shutdown();
	}
	
	@Test
	public void testWithEventTest1() throws InterruptedException {
		Assert.assertEquals(110, eventCenter.fireEvent(this, new EventInfo("test1").setArgs(new Object[]{1,2,3}), 110));
		Thread.sleep(100);
		Mockito.verify(listener1, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		Mockito.verify(listener2, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		
		Assert.assertEquals(1, listenerRef.listener1.intValue());
		Assert.assertEquals(110, listenerRef.listener1Result.intValue());
		Assert.assertEquals(1, listenerRef.listener2.intValue());
	}
	
	@Test
	public void testWithEventTest2() throws InterruptedException{
		Assert.assertEquals(110, eventCenter.fireEvent(this, new EventInfo("test2").setArgs(new Object[]{1,2,3}), 110));
		Thread.sleep(100);
		Mockito.verify(listener3, Mockito.atLeastOnce()).onObserved(Mockito.any(CommonEventSource.class));
		
		Assert.assertNull(listenerRef.listener3);
		Thread.sleep(listener2Sleep);
		Assert.assertEquals(1, listenerRef.listener3.intValue());
	}
	
	@Test
	public void testWithSync(){
		Assert.assertEquals(120, eventCenter.fireEvent(this, new EventInfo("test3").setArgs(new Object[]{1,2,3}).setAsync(false), 110));
	}
	
	@Test(expected=SyncListenerMoreThanOneException.class)
	public void testWithMultSync(){
		eventCenter.fireEvent(this, new EventInfo("test4").setArgs(new Object[]{1,2,3}).setAsync(false), 110);
	}
	
	@ListenerBind("test1")
	class Listener1 implements EventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			listenerRef.listener1 = evt.getArg(0, Integer.class);
			listenerRef.listener1Result = (Integer)evt.getResult();
		}
		
	}
	
	@ListenerBind("test1")
	class Listener2 implements EventListener {

		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			listenerRef.listener2 = evt.getArg(0, Integer.class);
		}
		
	}
	
	@ListenerBind("test2")
	class Listener3 implements EventListener {
		
		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			
			try{
				Thread.sleep(listener2Sleep);
			}catch(Exception e){
				e.printStackTrace();
			}
			listenerRef.listener3 = evt.getArg(0, Integer.class);
		}
		
	}
	
	@ListenerBind("test3")
	class Listener4 implements EventListener {
		
		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			
			try{
				Thread.sleep(listener2Sleep);
			}catch(Exception e){
				e.printStackTrace();
			}
			evt.setSyncResult(120);
		}
		
	}
	
	@ListenerBind("test4")
	class Listener5 implements EventListener {
		
		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			
			try{
				Thread.sleep(listener2Sleep);
			}catch(Exception e){
				e.printStackTrace();
			}
			evt.setSyncResult(120);
		}
		
	}
	
	@ListenerBind("test4")
	class Listener6 implements EventListener {
		
		@Override
		public void onObserved(CommonEventSource source) {
			CommonEventSource evt = source;
			
			try{
				Thread.sleep(listener2Sleep);
			}catch(Exception e){
				e.printStackTrace();
			}
			evt.setSyncResult(120);
		}
		
	}
	
	class ListenerRef{
		private Integer listener1;
		
		private Integer listener2;
		
		private Integer listener3;
		
		private Integer listener1Result;
	}

}
