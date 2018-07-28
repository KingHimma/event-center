package eventcenter.api.async.simple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eventcenter.api.EventCenterConfig;

public class TestSimpleQueueEventContainerFactory {

	private SimpleQueueEventContainerFactory factory;
	
	@Before
	public void setUp(){
		factory = new SimpleQueueEventContainerFactory();
	}
	
	@Test
	public void test1() {
		EventCenterConfig config = new EventCenterConfig();
		SimpleQueueEventContainer container = (SimpleQueueEventContainer)factory.createContainer(config);
		Assert.assertEquals(0, container.getThreadPool().getCorePoolSize());
		Assert.assertEquals(100, container.getThreadPool().getMaximumPoolSize());
		Assert.assertNotNull(container.getQueue());
	}
	
	@Test
	public void test2(){
		EventCenterConfig config = new EventCenterConfig();
		factory.setCorePoolSize(10);
		factory.setMaximumPoolSize(200);
		factory.setQueueCapacity(1000);
		SimpleQueueEventContainer container = (SimpleQueueEventContainer)factory.createContainer(config);
		Assert.assertEquals(10, container.getThreadPool().getCorePoolSize());
		Assert.assertEquals(200, container.getThreadPool().getMaximumPoolSize());
		Assert.assertNotNull(container.getQueue());
	}

}
