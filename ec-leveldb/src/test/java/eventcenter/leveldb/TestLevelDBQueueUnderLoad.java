package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventSourceBase;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class TestLevelDBQueueUnderLoad {

	private LevelDBQueue queue;

	LevelDBPersistenceAdapter adapter;
	
	private QueueMiddleComponent queueMiddle;

	File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb");

	JniDBFactory factory = new JniDBFactory();

	DefaultEventCenter eventCenter;

	public TestLevelDBQueueUnderLoad(){
		//org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		eventCenter.startup();
		adapter = new LevelDBPersistenceAdapter();
		queueMiddle = new QueueMiddleComponent(adapter);
		adapter.setDirPath(dir);
		//adapter.setPageSize(1);
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
		factory.destroy(dir, new Options());
	}
	
	/**
	 * 压力测试
	 * @throws InterruptedException 
	 */
	@Test
	public void testUnderLoad() throws InterruptedException{
		final List<String> uuids = Collections.synchronizedList(new ArrayList<String>());
		Test4Task t1 = new Test4Task(uuids);
		Test4Task t2 = new Test4Task(uuids);
		Test4Task t3 = new Test4Task(uuids);
		Test4Task t4 = new Test4Task(uuids);
		Test4Task t5 = new Test4Task(uuids);
		Test4Task t6 = new Test4Task(uuids);
		Test4Task t7 = new Test4Task(uuids);
		Test4Task t8 = new Test4Task(uuids);
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
		// 测试时间
		long total = 1000;
		long start = System.currentTimeMillis();
		int produced = 0;
		while(total >= (System.currentTimeMillis() - start)){
			queue.offer(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
			produced++;
		}
		
		System.out.println("total produced:" + produced);
		System.out.println("total consumed:" + uuids.size());
		System.out.println("wait for consumed end");
		while(uuids.size() < produced){
			System.out.println("current consumed size:" + uuids.size());
			Thread.sleep(1000);
		}
		t1.shutdown();
		t2.shutdown();
		t3.shutdown();
		t4.shutdown();
		t5.shutdown();
		t6.shutdown();
		t7.shutdown();
		t8.shutdown();
		System.out.println("current consumed size:" + uuids.size());
		System.out.println("total send:" + uuids.size());
		System.out.println("total produced:" + produced);
		System.out.println("total took:" + (System.currentTimeMillis() - start) + " ms.");
	}
	
	@Test
	public void testAdapterUnderLoad() throws InterruptedException{
		final List<String> uuids = Collections.synchronizedList(new ArrayList<String>());
		TestAdapterTask t1 = new TestAdapterTask(uuids);
		TestAdapterTask t2 = new TestAdapterTask(uuids);
		TestAdapterTask t3 = new TestAdapterTask(uuids);
		TestAdapterTask t4 = new TestAdapterTask(uuids);
		TestAdapterTask t5 = new TestAdapterTask(uuids);
		TestAdapterTask t6 = new TestAdapterTask(uuids);
		TestAdapterTask t7 = new TestAdapterTask(uuids);
		TestAdapterTask t8 = new TestAdapterTask(uuids);
		TestAdapterTask t9 = new TestAdapterTask(uuids);
		TestAdapterTask t10 = new TestAdapterTask(uuids);
		TestAdapterTask t11 = new TestAdapterTask(uuids);
		TestAdapterTask t12 = new TestAdapterTask(uuids);
		Thread l1 = new Thread(t1);
		Thread l2 = new Thread(t2);
		Thread l3 = new Thread(t3);
		Thread l4 = new Thread(t4);
		Thread l5 = new Thread(t5);
		Thread l6 = new Thread(t6);
		Thread l7 = new Thread(t7);
		Thread l8 = new Thread(t8);
		Thread l9 = new Thread(t9);
		Thread l10 = new Thread(t10);
		Thread l11 = new Thread(t11);
		Thread l12 = new Thread(t12);
		l1.start();
		l2.start();
		l3.start();
		l4.start();
		l5.start();
		l6.start();
		l7.start();
		l8.start();
		l9.start();
		l10.start();
		l11.start();
		l12.start();
		// 测试时间
		long total = 10000;
		long start = System.currentTimeMillis();
		int produced = 0;
		List<String> producedUuids = new ArrayList<String>();
		while(total >= (System.currentTimeMillis() - start)){			
			try {
				String uuid = UUID.randomUUID().toString();
				queueMiddle.save(new CommonEventSource(this, uuid, "test", null, null, null));
				produced++;
				producedUuids.add(uuid);
			} catch (PersistenceException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("total produced:" + produced);
		System.out.println("total consumed:" + uuids.size());
		System.out.println("wait for consumed end");
		while(uuids.size() < produced){
			System.out.println("current consumed size:" + uuids.size() + ",wc:" + printCursor(queueMiddle.writeCursor) + ",rc" + printCursor(queueMiddle.readCursor));
			Thread.sleep(1000);
			if(uuids.size() == produced - 1){
				analyse(producedUuids, uuids);
			}
		}
		t1.shutdown();
		t2.shutdown();
		t3.shutdown();
		t4.shutdown();
		t5.shutdown();
		t6.shutdown();
		t7.shutdown();
		t8.shutdown();
		t9.shutdown();
		t10.shutdown();
		t11.shutdown();
		t12.shutdown();
		System.out.println("current consumed size:" + uuids.size() + ",wc:" + printCursor(queueMiddle.writeCursor) + ",rc" + printCursor(queueMiddle.readCursor));
		System.out.println("total send:" + uuids.size());
		System.out.println("total produced:" + produced);
		System.out.println("total took:" + (System.currentTimeMillis() - start) + " ms.");
	}
	
	private void analyse(List<String> produced, List<String> consumed){
		Map<String, Integer> consumedMap = new HashMap<String, Integer>();
		for(String c : consumed){
			consumedMap.put(c, 1);
		}
		
		if(consumedMap.size() != consumed.size()){
			System.err.println("消费了重复的消息");
		}
		int index = 0;
		for(String p : produced){
			if(!consumedMap.containsKey(p)){
				System.out.println("未消费掉的事件:index:" + index + ",uuid:" + p);
			}
			index++;
		}
	}
	
	private String printCursor(LevelDBCursor c){
		return new StringBuilder("").append(c.getPageNo()).append(",").append(c.getIndex()).toString();
	}
	
	class Test3Task implements Runnable{

		private EventSourceBase evt;
		
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

		private EventSourceBase evt;
		
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
	
	class TestAdapterTask implements Runnable {
		
		private final List<String> uuids;
		
		private volatile boolean flag = true;
		
		public TestAdapterTask(List<String> uuids){
			this.uuids = uuids;
		}
		
		@Override
		public void run() {
			while(flag){
				List<EventSourceWrapper> list = null;
				try {
					list = queueMiddle.pop(1);
				} catch (PersistenceException e1) {
					e1.printStackTrace();
				}
				if(null == list || list.size() == 0){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					for(EventSourceBase evt : list){
						if(evt == null){
							System.err.println("发现一个空数据，当前读取页数:" + printCursor(queueMiddle.readCursor));
							continue;
						}
						uuids.add(evt.getEventId());
					}
				}
			}
		}
		
		public void shutdown(){
			flag = false;
		}
	}

}
