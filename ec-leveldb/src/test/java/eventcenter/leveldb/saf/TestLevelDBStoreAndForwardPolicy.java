package eventcenter.leveldb.saf;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventInfo;
import eventcenter.api.async.EventQueue;
import eventcenter.leveldb.LevelDBQueue;
import eventcenter.remote.EventInfoSource;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.Target;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.saf.TransmissionException;
import eventcenter.remote.saf.simple.SimpleEventForward;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liumingjian on 2017/1/3.
 */
public class TestLevelDBStoreAndForwardPolicy {

    private LevelDBStoreAndForwardPolicy policy;

    File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "testLevelDBSAF");

    SimpleEventForward eventForward;

    LevelDBQueue queue1;

    LevelDBQueue queue2;

    PublisherGroup group1;

    PublisherGroup group2;

    EventTransmission eventTransmission1;

    EventTransmission eventTransmission2;

    volatile boolean error = true;

    public TestLevelDBStoreAndForwardPolicy(){
        //org.apache.log4j.BasicConfigurator.configure();

    }

    @Before
    public void setUp() throws Exception {
        if(!dir.exists()){
            dir.mkdir();
        }
        policy = new LevelDBStoreAndForwardPolicy();
        policy.setPath(dir.getPath());
        policy.setStoreOnSendFail(true);
        policy.setCheckInterval(500L);
        eventForward = (SimpleEventForward)policy.createEventForward();
        queue1 = (LevelDBQueue)policy.createEventQueue("name1");
        queue2 = (LevelDBQueue)policy.createEventQueue("name2");
        eventTransmission1 = new SampleEventTransmission();
        group1 = new PublisherGroup(eventTransmission1);
        group1.setGroupName("name1");
        group1.setRemoteEvents("*");
        eventTransmission2 = new SampleEventTransmission();
        group2 = new PublisherGroup(eventTransmission2);
        group2.setGroupName("name2");
        group2.setRemoteEvents("*");
        Map<PublisherGroup, EventQueue> map = new HashMap<PublisherGroup, EventQueue>();
        map.put(group1, queue1);
        map.put(group2, queue2);
        error = true;
        eventForward.startup(map);
    }

    @After
    public void tearDown() throws Exception {
        eventForward.shutdown();
        queue1.close();
        queue2.close();
    }

    @Test(expected = TransmissionException.class)
    public void test1() throws Exception {
        eventForward.forward(group1, createEvent("test1"));
    }

    @Test
    public void test2() throws Exception {
        final int count = 10;
        for(int i = 0;i < count;i++){
            queue1.offer(createEvent("test"));
        }
        for(int i = 0;i < count;i++){
            queue2.offer(createEvent("test"));
        }
        Thread.sleep(200);
        Assert.assertEquals(count, queue1.enqueueSize());
        Assert.assertEquals(count, queue2.enqueueSize());
    }

    @Test
    public void test3() throws Exception {
        error = false;
        final int count = 10;
        for(int i = 0;i < count;i++){
            queue1.offer(createEvent("test"));
            queue2.offer(createEvent("test"));
        }
        Thread.sleep(1000);
        Assert.assertEquals(0, queue1.enqueueSize());
        Assert.assertEquals(0, queue2.enqueueSize());
    }

    /**
     * 测试leveldb索引文件的生成和删除
     *
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        error = false;
        final int count = 10000;
        for(int i = 0;i < count;i++){
            queue1.offer(createEvent("test"));
            queue2.offer(createEvent("test"));
        }
        Thread.sleep(1000);
        Assert.assertEquals(0, queue1.enqueueSize());
        Assert.assertEquals(0, queue2.enqueueSize());
    }

    /**
     * 休眠20秒让housekeeping进行索引删除
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        error = false;
        Thread.sleep(20000);
    }

    CommonEventSource createEvent(String eventName){
        EventInfoSource source = new EventInfoSource();
        source.setEventInfo(new EventInfo(eventName));
        source.setTarget(new Target(this.getClass().getName()));
        return source;
    }

    class SampleEventTransmission implements EventTransmission {

        SampleEventTransmission(){

        }

        @Override
        public boolean checkHealth() {
            return !error;
        }

        @Override
        public void asyncTransmission(Target target, EventInfo eventInfo, Object result) {
            if(error)
                throw new RuntimeException();
        }
    }
}