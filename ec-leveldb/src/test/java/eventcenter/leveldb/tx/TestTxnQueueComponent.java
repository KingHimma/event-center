package eventcenter.leveldb.tx;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.tx.EventTxnStatus;
import eventcenter.api.tx.ResumeTxnHandler;
import eventcenter.leveldb.EventSourceWrapper;
import org.fusesource.leveldbjni.JniDBFactory;
import org.fusesource.leveldbjni.internal.NativeDB;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liumingjian on 2016/12/28.
 */
public class TestTxnQueueComponent {

    private TxnQueueComponent component;

    DB db;

    JniDBFactory factory = new JniDBFactory();

    File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "leveldbtx");

    Options options = new Options();

    static{
        NativeDB.LIBRARY.load();
    }

    @Before
    public void setUp() throws Exception {
        options.createIfMissing(true);
        db = factory.open(dir, options);
        component = new TxnQueueComponent("test", db);
        component.open();
    }

    @After
    public void tearDown() throws Exception {
        if(null == db)
            return ;
        component.shutdown();
        db.close();
        factory.destroy(dir, options);
    }

    @Test
    public void testGetTxnStatus1() throws Exception {
        final String eventId = UUID.randomUUID().toString();
        final String txnId = UUID.randomUUID().toString();
        LevelDBEventTxnStatus status = (LevelDBEventTxnStatus)component.getTxnStatus(eventId, SampleListener.class, txnId);
        Assert.assertEquals(eventId, status.getEventId());
        Assert.assertEquals(SampleListener.class, status.getListenerType());
        Assert.assertEquals(1, status.getPageNo().intValue());
        Assert.assertNotNull(status.getBucketId());
        Assert.assertNotNull(status.getStart());
        Assert.assertNotNull(component.bucketGroup.findStatusByBucketTxnId(status.getBucketTxnId()));

        component.shutdown();
        db.close();
        setUp();
        status = (LevelDBEventTxnStatus)component.getTxnStatus(null, SampleListener.class, txnId);
        Assert.assertNotNull(status);
        Assert.assertEquals(eventId, status.getEventId());
    }

    @Test
    public void testCommit1() throws Exception {
        final String eventId = UUID.randomUUID().toString();
        final String txnId = UUID.randomUUID().toString();
        LevelDBEventTxnStatus status = (LevelDBEventTxnStatus)component.getTxnStatus(eventId, SampleListener.class, txnId);
        component.commit(status);
        WriteBatch wb = db.createWriteBatch();
        try {
            /*String index = component.bucketGroup.discardBucket.pop(wb);
            Assert.assertNotNull(index);*/
            Assert.assertNull(component.bucketGroup.txnBucket.popByIndex(wb, txnId));
            Assert.assertNull(component.bucketGroup.findStatusByBucketTxnId(status.getBucketTxnId()));
        }finally{
            db.write(wb);
            wb.close();
        }
    }

    @Test(expected = IllegalAccessException.class)
    public void testCommit2() throws Exception {
        final String eventId = UUID.randomUUID().toString();
        final String txnId = UUID.randomUUID().toString();
        LevelDBEventTxnStatus status = (LevelDBEventTxnStatus)component.getTxnStatus(eventId, SampleListener.class, txnId);
        component.commit(status);
        component.commit(status);
    }

    @Test
    public void testCommit3() throws Exception {
        final int count = 5000;
        List<String> deletedTxnIds = new ArrayList<String>(10 + 1);
        for(int i = 0;i < count;i++) {
            String eventId = UUID.randomUUID().toString();
            String txnId = UUID.randomUUID().toString();
            LevelDBEventTxnStatus status = (LevelDBEventTxnStatus) component.getTxnStatus(eventId, SampleListener.class, txnId);
            component.commit(status);
            deletedTxnIds.add(status.getBucketTxnId());
        }
        //Assert.assertEquals(component.bucketGroup.discardBucket.capacity(), component.bucketGroup.discardBucket.count());
        for(String deletedTxnId : deletedTxnIds) {
            Assert.assertNull(LevelDBBucket.get(db, component.buildTxnKey(deletedTxnId), LevelDBEventTxnStatus.class));
        }
    }

    /*@Test
    public void testHouseKeeping1() throws Exception {
        final int count = 200;
        List<String> txnIds = new ArrayList<String>(count + 1);
        List<String> bucketTxnIds = new ArrayList<String>(txnIds.size());
        for(int i = 0;i < count;i++) {
            String eventId = UUID.randomUUID().toString();
            String txnId = UUID.randomUUID().toString();
            LevelDBEventTxnStatus status = (LevelDBEventTxnStatus) component.getTxnStatus(eventId, SampleListener.class, txnId);
            bucketTxnIds.add(status.getBucketTxnId());
            component.commit(status);
        }
        Assert.assertEquals(count, component.bucketGroup.discardBucket.count());
        component.houseKeeping();
        Assert.assertEquals(0, component.bucketGroup.discardBucket.count());
        for(String bucketTxnId : bucketTxnIds){
            Assert.assertNull(LevelDBBucket.get(db, component.buildTxnKey(bucketTxnId), LevelDBEventTxnStatus.class));
        }
    }*/

    @Test
    public void testResumeTxn1() throws Exception {
        final int count = 20;
        final List<String> txnIds = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            String eventId = UUID.randomUUID().toString();
            String txnId = UUID.randomUUID().toString();
            component.getTxnStatus(eventId, SampleListener.class, txnId);
            txnIds.add(txnId);
            CommonEventSource evt = new CommonEventSource(this, txnId, "test", null, null, null);
            EventSourceWrapper wrapper = new EventSourceWrapper(txnId, evt);
            LevelDBBucket.set(db, component.wrapperKey(txnId), wrapper);
        }
        component.shutdown();
        db.close();

        setUp();
        final AtomicInteger counter = new AtomicInteger(0);
        component.resumeTxn(new ResumeTxnHandler() {
            @Override
            public void resume(EventTxnStatus status, EventSourceBase event) {
                Assert.assertEquals(status.getTxnId(), txnIds.get(counter.getAndIncrement()));
            }
        });
        Assert.assertEquals(count, counter.get());
    }

    @Test(expected = IllegalAccessException.class)
    public void testResumeTxn2() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String txnId = UUID.randomUUID().toString();
        component.getTxnStatus(eventId, SampleListener.class, txnId);
        component.resumeTxn(new ResumeTxnHandler() {
            @Override
            public void resume(EventTxnStatus status, EventSourceBase event) {

            }
        });
    }

    class SampleListener implements EventListener {

        @Override
        public void onObserved(EventSourceBase source) {

        }
    }
}