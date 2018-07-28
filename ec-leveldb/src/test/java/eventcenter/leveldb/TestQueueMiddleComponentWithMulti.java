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
import java.util.concurrent.atomic.AtomicLong;

public class TestQueueMiddleComponentWithMulti {

	LevelDBPersistenceAdapter adapter;

	private LevelDBQueue queue1;
	
	private QueueMiddleComponent queueMiddle1;

	private LevelDBQueue queue2;
	
	private QueueMiddleComponent queueMiddle2;

	private File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb");

	JniDBFactory factory = new JniDBFactory();

	private DefaultEventCenter eventCenter;

	public TestQueueMiddleComponentWithMulti(){
		//org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		eventCenter.startup();
		adapter = new LevelDBPersistenceAdapter();
		adapter.setDirPath(dir);
		queueMiddle1 = new QueueMiddleComponent(adapter, "q1");
		queue1 = new LevelDBQueue(queueMiddle1);
		LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queue1);
		strategy.setCheckInterval(1000);
		queue1.setHouseKeepingStrategy(strategy);
		queue1.open();
		
		queueMiddle2 = new QueueMiddleComponent(adapter, "q2");
		queue2 = new LevelDBQueue(queueMiddle2);
		strategy = new LimitReadHouseKeepingStrategy(queue2);
		strategy.setCheckInterval(1000);
		queue2.setHouseKeepingStrategy(strategy);
		queue2.open();
		
		queueMiddle1.clear();
		queueMiddle1.load();
		queueMiddle2.load();
	}
	
	@After
	public void tearDown() throws Exception{
		queueMiddle1.clear();
		queueMiddle2.clear();
		queue1.close();
		queue2.close();
		eventCenter.shutdown();
		factory.destroy(dir, new Options());
	}
	
	/**
	 * 压力测试
	 * @throws InterruptedException 
	 */
	@Test
	public void testUnderLoad() throws InterruptedException{
		final Set<String> uuids1 = Collections.synchronizedSet(new HashSet<String>());
		final Set<String> uuids2 = Collections.synchronizedSet(new HashSet<String>());
		List<Test4QueueTask> tasks1 = createListenThread(queue1, uuids1);
		List<Test4QueueTask> tasks2 = createListenThread(queue2, uuids2);
		// 测试时间
		long total = 1000;
		long start = System.currentTimeMillis();
		int produced = 0;
		final AtomicLong counter = new AtomicLong(0);
		while(total >= (System.currentTimeMillis() - start)){
			queue1.offer(new CommonEventSource(this, String.valueOf(counter.incrementAndGet()), "test", null, null, null));
			queue2.offer(new CommonEventSource(this, String.valueOf(counter.incrementAndGet()), "test", null, null, null));
			produced++;
		}
		
		System.out.println("total produced:" + produced);
		System.out.println("total queue1 consumed:" + uuids1.size());
		System.out.println("total queue2 consumed:" + uuids2.size());
		System.out.println("wait for consumed end");
		long totalTook = 5000;
		start = System.currentTimeMillis();
		while(uuids1.size() < produced || uuids2.size() < produced){
			System.out.println("current queue1 consumed size:" + uuids1.size());
			System.out.println("current queue2 consumed size:" + uuids2.size());
			Thread.sleep(1000);
			if((System.currentTimeMillis() - start) >= totalTook)
				break;
		}
		shutdownThreads(tasks1);
		shutdownThreads(tasks2);
		System.out.println("current queue1 consumed size:" + uuids1.size());
		System.out.println("current queue2 consumed size:" + uuids2.size());
		System.out.println("total queue1 send:" + uuids1.size());
		System.out.println("total queue2 send:" + uuids2.size());
		System.out.println("total produced:" + produced);
		System.out.println("total took:" + (System.currentTimeMillis() - start) + " ms.");
	}
	
	private List<Test4QueueTask> createListenThread(LevelDBQueue queue, Set<String> uuids){
		int count = 8;
		List<Test4QueueTask> list = new ArrayList<Test4QueueTask>(count + 1);
		for(int i = 0;i < count;i++){
			Test4QueueTask task = new Test4QueueTask(queue, uuids);
			list.add(task);
			new Thread(task).start();
		}
		return list;
	}
	
	private void shutdownThreads(List<Test4QueueTask> threads){
		for(Test4QueueTask t : threads){
			t.shutdown();
		}
	}
	
	class Test4QueueTask implements Runnable{

		private EventSourceBase evt;
		
		private final Set<String> uuids;
		
		private volatile boolean flag = true;
		
		private final LevelDBQueue queue;
		
		public Test4QueueTask(LevelDBQueue queue, Set<String> uuids){
			this.uuids = uuids;
			this.queue = queue;
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
					if(uuids.contains(evt.getEventId())){
						System.err.println("重复读取消息：" + evt.getEventId());
						continue;
					}
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
