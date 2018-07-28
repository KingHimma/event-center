package eventcenter.leveldb.tx;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventInfo;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.leveldb.LevelDBContainer;
import eventcenter.leveldb.LevelDBContainerFactory;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.Options;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试开启事务模式的容器
 * Created by liumingjian on 2016/12/30.
 */
public class TestLevelDBContainerWithTx {

    JniDBFactory factory = new JniDBFactory();

    DefaultEventCenter eventCenter;

    TestListener listener1 = new TestListener();

    TestListener2 listener2 = new TestListener2();

    File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "ecleveldb");

    List<String> eventIds = Collections.synchronizedList(new ArrayList<String>());

    AtomicInteger counter = new AtomicInteger(0);

    long listenerSleepTime = 2000;

    public TestLevelDBContainerWithTx(){
        org.apache.log4j.BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        eventCenter = new DefaultEventCenter();
        TransactionConfig txnConfig = new TransactionConfig();
        LevelDBContainerFactory levelDBContainerFactory = new LevelDBContainerFactory();
        levelDBContainerFactory.setPath(dir.getPath());
        levelDBContainerFactory.setTransactionConfig(txnConfig);
        levelDBContainerFactory.setCorePoolSize(1);
        levelDBContainerFactory.setMaximumPoolSize(1);
        levelDBContainerFactory.setReadLimitSize(10);
        EventCenterConfig ecConfig = new EventCenterConfig();
        ecConfig.putCommonListener("test", listener1);
        ecConfig.putCommonListener("test2", listener2);
        ecConfig.setQueueEventContainerFactory(levelDBContainerFactory);
        eventCenter.setEcConfig(ecConfig);

        eventCenter.startup();
    }

    @After
    public void tearDown() throws Exception {
        eventCenter.shutdown();
        factory.destroy(dir, new Options());
        eventIds.clear();
    }

    @Test
    public void testFireAndConsumed1() throws Exception {
        final int count = 10;
        for(int i = 0;i < count;i++) {
            eventCenter.fireEvent(this, new EventInfo("test"), null);
        }
        Thread.sleep(2000);
        Assert.assertEquals(count, eventIds.size());
    }

    @Test
    public void testFireAndConsumed2() throws Exception {
        final int count = 10;
        for(int i = 0;i < count;i++) {
            eventCenter.fireEvent(this, new EventInfo("test2"), null);
        }
        eventCenter.shutdown(); // 直接关闭事件中心
        listenerSleepTime = 100;
        Thread.sleep(100);
        System.out.println("关闭事件中心成功");
        System.out.println("当前执行的事件数:" + counter.get());
        // 然后重新加载
        eventCenter.startup();
        System.out.println("启动事件中心成功");
        Thread.sleep(2000);
        Assert.assertEquals(count, counter.get());
    }

    @Test
    public void testHouseKeeping1() throws Exception {
        listenerSleepTime = 0;
        final int count = 10;
        for(int i = 0;i < count;i++) {
            eventCenter.fireEvent(this, new EventInfo("test2"), null);
        }
        Thread.sleep(11500);
        LevelDBContainer container = (LevelDBContainer)eventCenter.getAsyncContainer();
        Assert.assertEquals(0, container.queueSize());
    }

    public void fireEvents(int count){
        for(int i = 0;i < count;i++) {
            eventCenter.fireEvent(this, new EventInfo("test2"), null);
        }
        System.out.println("事件发送成功:" + count);
    }

    public void shutdownEventCenter() throws Exception {
        eventCenter.shutdown(); // 直接关闭事件中心
    }

    class TestListener implements EventListener {

        @Override
        public void onObserved(EventSourceBase source) {
            eventIds.add(source.getEventId());
        }

    }

    class TestListener2 implements EventListener {

        @Override
        public void onObserved(EventSourceBase source) {
            try {
                Thread.sleep(listenerSleepTime);
                System.out.println("当前执行的事件数:" + counter.incrementAndGet());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
