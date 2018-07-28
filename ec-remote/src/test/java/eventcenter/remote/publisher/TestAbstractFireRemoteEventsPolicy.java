package eventcenter.remote.publisher;

import eventcenter.api.*;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.Target;
import eventcenter.remote.Target;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by liumingjian on 2016/11/17.
 */
public class TestAbstractFireRemoteEventsPolicy {

    PublisherGroup group1;

    PublisherGroup group2;

    PublishEventCenter eventCenter;

    List<String> localEventIds;

    List<String> eventIds;

    List<String> excepEventIds;

    EventTransmission group1Trans;

    EventTransmission group2Trans;

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
        group2Trans = mock(EventTransmission.class);
        group2 = new PublisherGroup(group2Trans);
        group2.setGroupName("group2");
        group2.setRemoteUrl("127.0.0.1:6666");
        group2.setRemoteEvents("test2");
        // 创建一个本地事件消费
        LocalPublisherGroup localGroup = new LocalPublisherGroup();
        localGroup.setRemoteEvents("test3");

        eventCenter = new PublishEventCenter();
        EventCenterConfig ecConfig = new EventCenterConfig();
        ecConfig.getModuleFilters().add(new SamplePublishFilter(eventIds, excepEventIds));
        CommonEventListenerConfig config = new CommonEventListenerConfig();
        config.getListeners().put("test3", Arrays.asList((EventListener)new SampleEventListener(localEventIds)));
        ecConfig.setEventListenerConfig(config);
        eventCenter.setAsyncFireRemote(false);
        eventCenter.setEcConfig(ecConfig);
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

        EventInfo event3 = new EventInfo("test3");
        eventCenter.fireEvent(this, event3, null);
        Thread.sleep(500);
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
        doThrow(RuntimeException.class).when(group1Trans).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
        eventCenter.fireEvent(this, event, null);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(event.getId(), eventIds.get(0));
        Assert.assertEquals(1, excepEventIds.size());
        Assert.assertEquals(event.getId(), excepEventIds.get(0));
        verify(group1Trans, atLeastOnce()).asyncTransmission(any(Target.class), any(EventInfo.class), anyObject());
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

    @ListenerBind("test3")
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