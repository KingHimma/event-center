package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.leveldb.utils.LevelDbUtils;
import org.junit.*;

import java.io.File;
import java.util.UUID;

public class TestQueueMiddleComponent {

	LevelDBPersistenceAdapter adapter;
	
	QueueMiddleComponent queueMiddle;

	DefaultEventCenter eventCenter;
	
	@Before
	public void setUp() throws Exception{
		eventCenter = new DefaultEventCenter();
		eventCenter.startup();
		adapter = new LevelDBPersistenceAdapter();
		queueMiddle = new QueueMiddleComponent(adapter);
		adapter.setDirPath(new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb"));
		queueMiddle.open();
	}
	
	@After
	public void tearDown() throws Exception{
		queueMiddle.close();
		eventCenter.shutdown();
	}
	
	/**
	 * just test write
	 * @throws PersistenceException
	 */
	@Test
	public void testWriteAndPop() throws PersistenceException {
		long writeIndex = queueMiddle.writeCursor.getIndex();
		long readIndex = queueMiddle.readCursor.getIndex();
		final String uuid = UUID.randomUUID().toString();
		String txnId = queueMiddle.save(new CommonEventSource(this, uuid, "test", null, null, null));
		Assert.assertEquals(writeIndex + 1, queueMiddle.writeCursor.getIndex());
		Assert.assertEquals(readIndex, queueMiddle.readCursor.getIndex());
		LevelDBCursor wc = queueMiddle.get(queueMiddle.buildKey(QueueMiddleComponent.KEY_WRITE_CURSOR), LevelDBCursor.class);
		LevelDBPage page = queueMiddle.getPage(queueMiddle.writeCursor.getPageNo());
		Assert.assertEquals(writeIndex + 1, wc.getIndex());
		Assert.assertEquals(txnId, page.getIndexes().get(page.getIndexes().size() - 1));
		EventSourceWrapper wrapper = queueMiddle.get(txnId, EventSourceWrapper.class);
		Assert.assertNotNull(wrapper);
		Assert.assertEquals(wrapper.getTxnId(), txnId);
		
		writeIndex = queueMiddle.writeCursor.getIndex();
		readIndex = queueMiddle.readCursor.getIndex();
		EventSourceWrapper evt = queueMiddle.pop();
		Assert.assertEquals(true, evt.getEvt() instanceof CommonEventSource);
		Assert.assertEquals(writeIndex, queueMiddle.writeCursor.getIndex());
		//Assert.assertEquals(readIndex + 1, adapter.readCursor.getIndex());
		/*LevelDBCursor rc = adapter.get(adapter.buildKey(EffecientLevelDBPersistenceAdapter.KEY_READ_CURSOR), LevelDBCursor.class);
		Assert.assertEquals(readIndex + 1, rc.getIndex());*/		
	}

	@Ignore
	@Test
	public void testWriteNextPage() throws PersistenceException{
		queueMiddle.setPageSize(1);
		long writeIndex = queueMiddle.writeCursor.getIndex();
		long writePageNo = queueMiddle.writeCursor.getPageNo();
		long readIndex = queueMiddle.readCursor.getIndex();
		long readPageNo = queueMiddle.readCursor.getPageNo();
		String uuid = UUID.randomUUID().toString();
		String txnId1 = queueMiddle.save(new CommonEventSource(this, uuid, "test", null, null, null));
		Assert.assertEquals(1, queueMiddle.writeCursor.getIndex());
		Assert.assertEquals(writePageNo, queueMiddle.writeCursor.getPageNo());
		Assert.assertEquals(readIndex, queueMiddle.readCursor.getIndex());
		Assert.assertEquals(readPageNo, queueMiddle.readCursor.getPageNo());
		LevelDBCursor wc = queueMiddle.get(queueMiddle.buildKey(QueueMiddleComponent.KEY_WRITE_CURSOR), LevelDBCursor.class);
		LevelDBPage page = queueMiddle.getPage(queueMiddle.writeCursor.getPageNo());
		Assert.assertEquals(1, wc.getIndex());
		Assert.assertEquals(txnId1, page.getIndexes().get(page.getIndexes().size() - 1));
		EventSourceWrapper wrapper = queueMiddle.get(txnId1, EventSourceWrapper.class);
		Assert.assertNotNull(wrapper);
		Assert.assertEquals(wrapper.getTxnId(), txnId1);
		
		writeIndex = queueMiddle.writeCursor.getIndex();
		readIndex = queueMiddle.readCursor.getIndex();
		readPageNo = queueMiddle.readCursor.getPageNo();
		EventSourceWrapper evt = queueMiddle.pop();
		Assert.assertEquals(true, evt.getEvt() instanceof CommonEventSource);
		Assert.assertEquals(writeIndex, queueMiddle.writeCursor.getIndex());
		System.out.println("当前读取游标页码：" + queueMiddle.readCursor.getPageNo() + ",index:" + queueMiddle.readCursor.getIndex());	
		
	}

	@Test
	public void testCount() throws PersistenceException {
		queueMiddle.setPageSize(10);
		queueMiddle.save(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		Assert.assertEquals(1, queueMiddle.count());
		for(int i = 0;i < 10;i++){
			queueMiddle.save(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		}
		Assert.assertEquals(11, queueMiddle.count());

		for(int i = 0;i < 5;i++){
			queueMiddle.save(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		}
		Assert.assertEquals(16, queueMiddle.count());

		queueMiddle.pop();
		Assert.assertEquals(15, queueMiddle.count());

		queueMiddle.pop(5);
		Assert.assertEquals(10, queueMiddle.count());

	}

	/**
	 * 测试houseKeeping是否可以正常删除sst文件
	 */
	@Test
	public void testHouseKeeping() throws Exception {
		int size = 1000000;
		for (int i = 0; i < size; i++) {
			queueMiddle.save(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		}
		System.out.println("save后：" + LevelDbUtils.generateSstFileStatisticsInfo(queueMiddle.adapter));
		for (int i = 0; i < size; i++) {
			queueMiddle.pop();
		}
		System.out.println("pop后：" + LevelDbUtils.generateSstFileStatisticsInfo(queueMiddle.adapter));
		queueMiddle.houseKeeping();
		System.out.println("houseKeeping后：" + LevelDbUtils.generateSstFileStatisticsInfo(queueMiddle.adapter));
		long start = System.currentTimeMillis();
		queueMiddle.adapter.getDb().compactRange(null, null);
		System.out.println("compact耗时" + (System.currentTimeMillis() - start) + "毫秒");
		System.out.println("compact后：" + LevelDbUtils.generateSstFileStatisticsInfo(queueMiddle.adapter));
	}

	/**
	 * 测试统计150m左右sst文件的性能
	 * @throws Exception
	 */
	@Test
	public void testSstFileStatisticsTime() throws Exception {
		int size = 1000000;
		for (int i = 0; i < size; i++) {
			queueMiddle.save(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
		}
		long start = System.currentTimeMillis();
		LevelDbUtils.SstFileStatisticsInfo sstFileStatisticsInfo = LevelDbUtils.generateSstFileStatisticsInfo(queueMiddle.adapter);
		System.out.println("统计sst文件耗时：" + (System.currentTimeMillis() - start) + "毫秒");
		System.out.println("pop后：" + sstFileStatisticsInfo);
	}
}
