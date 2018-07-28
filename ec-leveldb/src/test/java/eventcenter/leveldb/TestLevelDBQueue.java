package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventSourceBase;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestLevelDBQueue {

	private LevelDBQueue queue;

	LevelDBPersistenceAdapter adapter;
	
	private QueueMiddleComponent queueMiddle;

	private DefaultEventCenter eventCenter;
	
	public TestLevelDBQueue(){
		//org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		eventCenter.startup();
		adapter = new LevelDBPersistenceAdapter();
		queueMiddle = new QueueMiddleComponent(adapter);
		adapter.setDirPath(new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb"));
		queue = new LevelDBQueue(queueMiddle);
		LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queue);
		strategy.setCheckInterval(1000);
		queue.setHouseKeepingStrategy(strategy);
		queue.open();
		queueMiddle.clear();
	}
	
	@After
	public void tearDown() throws Exception{
		queueMiddle.clear();
		queue.close();
		eventCenter.shutdown();
	}
	
	@Test
	public void test1() {
		String uuid = UUID.randomUUID().toString();
		CommonEventSource evt = new CommonEventSource(this, uuid, "test", null, null, null);
		queue.offer(evt);
		EventSourceBase result = queue.transfer();
		Assert.assertEquals(uuid, result.getEventId());
		Assert.assertEquals(true, result instanceof CommonEventSource);
		
		uuid = UUID.randomUUID().toString();
		evt = new CommonEventSource(this, uuid, "test", null, null, null);
		queue.offer(evt);
		result = queue.transfer();
		Assert.assertEquals(uuid, result.getEventId());
		Assert.assertEquals(true, result instanceof CommonEventSource);
	}
	
	@Test
	public void test2() throws InterruptedException{
		final List<String> received = Collections.synchronizedList(new ArrayList<String>());
		final int count = 4;
		Thread listener = new Thread(new Runnable(){

			@Override
			public void run() {
				int index = 0;
				while(index < count){
					CommonEventSource evt = queue.transfer();
					received.add(evt.getEventId());
					System.out.println("received:" + evt.getEventId());
					index++;
				}
			}
			
		});
		listener.start();
		
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String uuid3 = UUID.randomUUID().toString();
		String uuid4 = UUID.randomUUID().toString();
		queue.offer(new CommonEventSource(this, uuid1, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid2, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid3, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid4, "test", null, null, null));
		Thread.sleep(100);
		Assert.assertEquals(4, received.size());
		Assert.assertEquals(uuid1, received.get(0));
		Assert.assertEquals(uuid2, received.get(1));
		Assert.assertEquals(uuid3, received.get(2));
		Assert.assertEquals(uuid4, received.get(3));
	}
	
	/**
	 * 测试多线程调用transfer
	 * @throws InterruptedException
	 */
	@Test
	public void test3() throws InterruptedException{
		final List<String> uuids = Collections.synchronizedList(new ArrayList<String>());
		Test3Task t1 = new Test3Task(uuids);
		Test3Task t2 = new Test3Task(uuids);
		Test3Task t3 = new Test3Task(uuids);
		Test3Task t4 = new Test3Task(uuids);
		Test3Task t5 = new Test3Task(uuids);
		Test3Task t6 = new Test3Task(uuids);
		Test3Task t7 = new Test3Task(uuids);
		Test3Task t8 = new Test3Task(uuids);
		Thread l1 = new Thread(t1);
		Thread l2 = new Thread(t2);
		Thread l3 = new Thread(t3);
		Thread l4 = new Thread(t4);
		Thread l5 = new Thread(t5);
		Thread l6 = new Thread(t6);
		Thread l7 = new Thread(t7);
		Thread l8 = new Thread(t8);
		l1.start();
		l2.start();
		l3.start();
		l4.start();
		l5.start();
		l6.start();
		l7.start();
		l8.start();
		String uuid1 = "1";
		String uuid2 = "2";
		String uuid3 = "3";
		String uuid4 = "4";
		String uuid5 = "5";
		String uuid6 = "6";
		String uuid7 = "7";
		String uuid8 = "8";
		queue.offer(new CommonEventSource(this, uuid1, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid2, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid3, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid4, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid5, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid6, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid7, "test", null, null, null));
		queue.offer(new CommonEventSource(this, uuid8, "test", null, null, null));
		Thread.sleep(500);
		Assert.assertNotNull(t1.getEvt());	
		Assert.assertNotNull(t2.getEvt());	
		Assert.assertNotNull(t3.getEvt());	
		Assert.assertNotNull(t4.getEvt());	
		Assert.assertNotNull(t5.getEvt());		
		Assert.assertNotNull(t6.getEvt());		
		Assert.assertNotNull(t7.getEvt());		
		Assert.assertNotNull(t8.getEvt());
		Assert.assertEquals(8, uuids.size());
		Assert.assertEquals(uuid1, uuids.get(0));
		Assert.assertEquals(uuid2, uuids.get(1));
		Assert.assertEquals(uuid3, uuids.get(2));
		Assert.assertEquals(uuid4, uuids.get(3));
		Assert.assertEquals(uuid5, uuids.get(4));
		Assert.assertEquals(uuid6, uuids.get(5));
		Assert.assertEquals(uuid7, uuids.get(6));
		Assert.assertEquals(uuid8, uuids.get(7));
	}
	
	/**
	 * 压力测试
	 * @throws InterruptedException 
	 */
	@Test
	public void test4() throws InterruptedException{
		final List<String> uuids = new ArrayList<String>();
		Test4Task t1 = new Test4Task(uuids);
		Test4Task t2 = new Test4Task(uuids);
		Thread l1 = new Thread(t1);
		Thread l2 = new Thread(t2);
		l1.start();
		l2.start();
		// 测试时间
		long total = 10000;
		long start = System.currentTimeMillis();
		int produced = 0;
		while(total >= (System.currentTimeMillis() - start)){
			queue.offer(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
			produced++;
		}
		t1.shutdown();
		t2.shutdown();
		System.out.println("total send:" + uuids.size());
		System.out.println("total produced:" + produced);
	}
	
	class Test3Task implements Runnable{

		private CommonEventSource evt;
		
		private final List<String> uuids;
		
		public Test3Task(List<String> uuids){
			this.uuids = uuids;
		}
		
		@Override
		public void run() {
			evt = queue.transfer();
			uuids.add(evt.getEventId());
		}

		public EventSourceBase getEvt() {
			return evt;
		}
		
	}
	
	class Test4Task implements Runnable{

		private CommonEventSource evt;
		
		private final List<String> uuids;
		
		private volatile boolean flag = true;
		
		public Test4Task(List<String> uuids){
			this.uuids = uuids;
		}
		
		@Override
		public void run() {
			while(flag){
				evt = queue.transfer();
				if(null == evt){
					System.err.println("transfer evt is null");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					uuids.add(evt.getEventId());
				}
			}
		}

		public EventSourceBase getEvt() {
			return evt;
		}
		
		public void shutdown(){
			flag = false;
		}
		
	}

}
