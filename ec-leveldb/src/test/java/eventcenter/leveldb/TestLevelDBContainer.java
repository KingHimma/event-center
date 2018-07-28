package eventcenter.leveldb;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventInfo;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.support.DefaultEventCenter;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestLevelDBContainer {

	private LevelDBContainer container;
	
	private EventCenterConfig config;

	private List<String> uuids;

	JniDBFactory factory = new JniDBFactory();

	DefaultEventCenter eventCenter;

	File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb");
	
	public TestLevelDBContainer(){
		//org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		uuids = new ArrayList<String>();

		LevelDBContainerFactory levelDBContainerFactory = new LevelDBContainerFactory();
		levelDBContainerFactory.setPath(dir.getPath());
		levelDBContainerFactory.setCorePoolSize(1);
		levelDBContainerFactory.setMaximumPoolSize(1);
		config = new EventCenterConfig();
		config.putCommonListener("test", new TestListener());
		config.putCommonListener("test2", new TestListener2(uuids));
		config.setQueueEventContainerFactory(levelDBContainerFactory);
		eventCenter.setEcConfig(config);
		eventCenter.startup();
		container = (LevelDBContainer)eventCenter.getAsyncContainer();
	}
	
	@After
	public void tearDown() throws Exception{
		eventCenter.shutdown();
		factory.destroy(dir, new Options());
	}
	
	@Test
	public void test1() throws InterruptedException {
		Assert.assertEquals(true, container.isIdle());
		EventInfo event = new EventInfo("test");
		String eventId = event.getId();
		eventCenter.fireEvent(this, event, null);
		System.out.println("offer event:" + eventId);
		Thread.sleep(1000);
		Assert.assertEquals(false, container.isIdle());
		event = new EventInfo("test");
		eventId = event.getId();
		eventCenter.fireEvent(this, event, null);
		System.out.println("offer event:" + eventId);
		Thread.sleep(10);
		Assert.assertEquals(false, container.isIdle());
		Thread.sleep(2200);
		Assert.assertEquals(true, container.isIdle());		
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

	class TestListener2 implements EventListener {

		private final List<String> uuids;
		
		public TestListener2(List<String> uuids){
			this.uuids = uuids;
		}
		
		@Override
		public void onObserved(EventSourceBase source) {
			this.uuids.add(source.getEventId());
		}
		
	}
}
