package eventcenter.leveldb;

import eventcenter.api.*;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.support.DefaultEventCenter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestLevelDBEventContainerFactory {

	DefaultEventCenter eventCenter;

	@Ignore
	@Test
	public void test() throws Exception {
		eventCenter = new DefaultEventCenter();
		eventCenter.startup();
		EventCenterConfig config = new EventCenterConfig();
		List<EventListener> listeners = new ArrayList<EventListener>();
		listeners.add(new TestListener());
		CommonEventListenerConfig listenerConfig = new CommonEventListenerConfig();
		listenerConfig.getListeners().put("test", listeners );
		config.loadCommonEventListenerConfig(listenerConfig);
		LevelDBContainerFactory factory = new LevelDBContainerFactory();
		factory.setCorePoolSize(1);
		factory.setMaximumPoolSize(1);
		QueueEventContainer container = factory.createContainer(config);
		container.startup();
		
		Assert.assertEquals(true, container.isIdle());
		container.send(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		Thread.sleep(200);
		Assert.assertEquals(false, container.isIdle());
		container.send(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		Thread.sleep(500);
		Assert.assertEquals(false, container.isIdle());
		Thread.sleep(1500);
		Assert.assertEquals(true, container.isIdle());	
		
		container.shutdown();
		eventCenter.shutdown();
	}
	
	class TestListener implements EventListener {

		@Override
		public void onObserved(EventSourceBase source) {
			try {
				System.out.println("begin consuming:" + source.getEventId());
				Thread.sleep(1000);
				System.out.println("consumed:" + source.getEventId());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
