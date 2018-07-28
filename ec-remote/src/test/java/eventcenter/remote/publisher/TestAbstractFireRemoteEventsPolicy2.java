package eventcenter.remote.publisher;

import eventcenter.api.*;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.Target;
import eventcenter.remote.saf.SAFPublishEventCenter;
import eventcenter.remote.saf.simple.SimpleStoreAndForwardPolicy;
import eventcenter.remote.Target;
import eventcenter.remote.saf.SAFPublishEventCenter;
import eventcenter.remote.saf.simple.SimpleStoreAndForwardPolicy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

/**
 * Created by liumingjian on 2016/11/17.
 */
public class TestAbstractFireRemoteEventsPolicy2 {

    PublisherGroup group1;

    PublisherGroup group2;

    SAFPublishEventCenter eventCenter;

    List<String> localEventIds;

    List<String> eventIds;

    List<String> excepEventIds;

    EventTransmission group1Trans;

    EventTransmission group2Trans;

    public TestAbstractFireRemoteEventsPolicy2(){
        org.apache.log4j.BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        eventIds = Collections.synchronizedList(new ArrayList<String>());
        excepEventIds = Collections.synchronizedList(new ArrayList<String>());
        localEventIds = Collections.synchronizedList(new ArrayList<String>());
        group1Trans = mock(EventTransmission.class);
        group1 = new PublisherGroup(group1Trans);
        group1.setGroupName("group1");
        group1.setRemoteUrl("127.0.0.1:8888");
        group1.setRemoteEvents("test1");
        when(group1Trans.checkHealth()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                System.out.println("check health");
                return true;
            }
        });
        group2Trans = mock(EventTransmission.class);
        group2 = new PublisherGroup(group2Trans);
        group2.setGroupName("group2");
        group2.setRemoteUrl("127.0.0.1:6666");
        group2.setRemoteEvents("test2");
        when(group2Trans.checkHealth()).thenReturn(true);
        // 创建一个本地事件消费
        LocalPublisherGroup localGroup = new LocalPublisherGroup();
        localGroup.setRemoteEvents("TestAbstractFireRemoteEventsPolicy2_test3");

        eventCenter = new SAFPublishEventCenter();
        EventCenterConfig ecConfig = new EventCenterConfig();
        ecConfig.getModuleFilters().add(new SamplePublishFilter(eventIds, excepEventIds));
        CommonEventListenerConfig config = new CommonEventListenerConfig();
        config.getListeners().put("TestAbstractFireRemoteEventsPolicy2_test3", Arrays.asList((EventListener) new SampleEventListener(localEventIds)));
        ecConfig.setEventListenerConfig(config);
        eventCenter.setAsyncFireRemote(false);
        eventCenter.setEcConfig(ecConfig);
        SimpleStoreAndForwardPolicy safPolicy = new SimpleStoreAndForwardPolicy();
        safPolicy.setCheckInterval(50L);
        safPolicy.setStoreOnSendFail(true);
        eventCenter.setSafPolicy(safPolicy);
        eventCenter.getEventPublisher().publish(Arrays.asList(localGroup, group1, group2));
        eventCenter.startup();
    }

    @After
    public void tearDown() throws Exception {
        eventCenter.shutdown();

    }

    /**
     * 测试fireRemoteEvent时，能够经过过滤器
     * @throws Exception
     */
    @Test
    public void testFireRemoteEvent() throws Exception {
        EventInfo event = new EventInfo("test1");
        eventCenter.fireEvent(this, event, null);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(event.getId(), eventIds.get(0));
        verify(group1Trans, atLeastOnce()).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());

        EventInfo event2 = new EventInfo("test2");
        eventCenter.fireEvent(this, event2, null);
        Assert.assertEquals(2, eventIds.size());
        Assert.assertEquals(event2.getId(), eventIds.get(1));
        verify(group2Trans, atLeastOnce()).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());

        EventInfo event3 = new EventInfo("TestAbstractFireRemoteEventsPolicy2_test3");
        eventCenter.fireEvent(this, event3, null);
        Thread.sleep(1000);
        Assert.assertEquals(1, localEventIds.size());
        Assert.assertEquals(event3.getId(), localEventIds.get(0));
    }

    /**
     * 测试fireRemoteEvent时，能够经过过滤器
     * @throws Exception
     */
    @Test
    public void testFireRemoteEventWithException() throws Exception {
        EventInfo event = new EventInfo("test1");
        doThrow(new RuntimeException("测试报错")).when(group1Trans).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
        eventCenter.fireEvent(this, event, null);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(event.getId(), eventIds.get(0));
        Assert.assertEquals(1, excepEventIds.size());
        Assert.assertEquals(event.getId(), excepEventIds.get(0));
        verify(group1Trans, atLeastOnce()).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
    }

    /**
     * 测试fireRemoteEvent时，能够经过过滤器，通过Store and forward机制，他能够通过重试恢复
     * @throws Exception
     */
    @Test
    public void testFireRemoteEventWithException2() throws Exception {
        EventInfo event = new EventInfo("test1");
        final AtomicInteger asyncCount = new AtomicInteger(0);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if(asyncCount.getAndIncrement() == 0)
                    throw new RuntimeException();
                return "";
            }
        }).when(group1Trans).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
        eventCenter.fireEvent(this, event, null);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(event.getId(), eventIds.get(0));
        Assert.assertEquals(1, excepEventIds.size());
        Assert.assertEquals(event.getId(), excepEventIds.get(0));
        verify(group1Trans, atLeastOnce()).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
        Thread.sleep(55); // 休眠55毫秒，等待看门狗再次触发asyncTransmission方法
        Assert.assertEquals(2, eventIds.size());
        Assert.assertEquals(event.getId(), eventIds.get(1));
        Assert.assertEquals(1, excepEventIds.size());
        Assert.assertEquals(event.getId(), excepEventIds.get(0));
    }

    class SamplePublishFilter implements PublishFilter {

        final List<String> eventIds;

        final List<String> excepEventIds;

        public SamplePublishFilter(List<String> eventIds, List<String> excepEventIds){
            this.eventIds = eventIds;
            this.excepEventIds = excepEventIds;
        }

        @Override
        public boolean afterSend(PublisherGroup group, Target target, EventInfo eventInfo, Object result, Exception e) {
            eventIds.add(eventInfo.getId());
            if(e != null){
                excepEventIds.add(eventInfo.getId());
            }
            return true;
        }
    }

    @ListenerBind("TestAbstractFireRemoteEventsPolicy2_test3")
    class SampleEventListener implements EventListener {

        final List<String> eventIds;

        public SampleEventListener(List<String> eventIds){
            this.eventIds = eventIds;
        }

        @Override
        public void onObserved(CommonEventSource source) {
            //System.out.println("local event consumed");
            this.eventIds.add(source.getEventId());
        }
    }
}