package eventcenter.leveldb;

import eventcenter.api.*;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liumingjian on 16/8/24.
 */
public class TestLevelDBContainer2 {

	private LevelDBContainer container;

	private EventCenterConfig config;

	private LevelDBQueue queue;

	LevelDBPersistenceAdapter adapter;

	private QueueMiddleComponent queueMiddle;

	private List<String> uuids;

	File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb");

	JniDBFactory factory = new JniDBFactory();

	DefaultEventCenter eventCenter;

	public TestLevelDBContainer2(){
		//org.apache.log4j.BasicConfigurator.configure();
	}

	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		uuids = new ArrayList<String>();
		adapter = new LevelDBPersistenceAdapter();
		queueMiddle = new QueueMiddleComponent(adapter);

		adapter.setDirPath(dir);
		queue = new LevelDBQueue(queueMiddle);
		LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queue);
		strategy.setCheckInterval(1000);
		queue.setHouseKeepingStrategy(strategy);
		config = new EventCenterConfig();
		List<EventListener> listeners = new ArrayList<EventListener>();
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		listeners.add(new Inner2TestListener(uuids));
		// 一个事件添加10个监听器
		CommonEventListenerConfig listenerConfig = new CommonEventListenerConfig();
		listenerConfig.getListeners().put("test", listeners );
		config.loadCommonEventListenerConfig(listenerConfig);
		eventCenter.setEcConfig(config);
		eventCenter.startup();
		container = new LevelDBContainer(config, queue);
		container.setCorePoolSize(1);
		container.setMaximumPoolSize(1);
		container.startup();
		queueMiddle.clear();
	}

	@After
	public void tearDown() throws Exception{
		queueMiddle.clear();
		container.shutdown();
		eventCenter.shutdown();
		factory.destroy(dir, new Options());
	}

	@Test
	public void test1() throws InterruptedException {
		Assert.assertEquals(true, container.isIdle());
		final int total = 10;
		for(int i = 0;i < total;i++) {
			String eventId = UUID.randomUUID().toString();
			queue.offer(new CommonEventSource(this, eventId, "test", null, null, null));
		}
		final int consumedCount = 10*total;
		final long start = System.currentTimeMillis();
		final long limitTime = total * 2000;
		// 限定5秒时间
		while((limitTime > (System.currentTimeMillis() - start)) && uuids.size() < consumedCount){
			Thread.sleep(100);
		}
		Assert.assertEquals(consumedCount, uuids.size());
	}

	class Inner2TestListener implements EventListener {

		private final List<String> uuids;

		public Inner2TestListener(List<String> uuids){
			this.uuids = uuids;
		}

		@Override
		public void onObserved(CommonEventSource source) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.uuids.add(source.getEventId());
			System.out.println(String.format("触发监听器,uuid:%s", source.getEventId()));
		}

	}
}
