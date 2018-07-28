package eventcenter.remote.subscriber;

import eventcenter.api.*;
import eventcenter.api.EventListener;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.remote.Target;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by liumingjian on 2016/11/18.
 */
public class TestEventSubscriber {

    EventSubscriber subscriber;

    DefaultEventCenter eventCenter;

    List<String> eventIds;

    AtomicBoolean consumedEvent;

    SampleSubscirberFilter filter;

    @Before
    public void setUp() throws Exception {
        consumedEvent = new AtomicBoolean(false);
        eventIds = Collections.synchronizedList(new ArrayList<String>());
        EventCenterConfig config = new EventCenterConfig();
        filter = new SampleSubscirberFilter(eventIds);
        config.getModuleFilters().add(filter);
        Map<String, List<EventListener>> listenerMap = new HashMap<String, List<EventListener>>();
        listenerMap.put("TestEventSubscriber", Arrays.asList((EventListener) new SampleEventListener()));
        CommonEventListenerConfig commConfig = new CommonEventListenerConfig();
        commConfig.setListeners(listenerMap);
        config.setEventListenerConfig(commConfig);
        eventCenter = new DefaultEventCenter();
        eventCenter.setEcConfig(config);
        eventCenter.startup();
        subscriber = new EventSubscriber(eventCenter);
    }

    @After
    public void tearDown() throws Exception {
        eventCenter.shutdown();

    }

    @Test
    public void testFilter() throws Exception {
        Target target = new Target();
        EventInfo eventInfo = new EventInfo("TestEventSubscriber");
        subscriber.asyncTransmission(target, eventInfo, null);
        Thread.sleep(500);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(eventInfo.getId(), eventIds.get(0));
        Assert.assertTrue(consumedEvent.get());
    }

    @Test
    public void testFilter2() throws Exception {
        filter.returnTrue = false;
        Target target = new Target();
        EventInfo eventInfo = new EventInfo("TestEventSubscriber");
        subscriber.asyncTransmission(target, eventInfo, null);
        Thread.sleep(500);
        Assert.assertEquals(1, eventIds.size());
        Assert.assertEquals(eventInfo.getId(), eventIds.get(0));
        Assert.assertFalse(consumedEvent.get());

    }

    class SampleSubscirberFilter implements SubscribFilter {

        final List<String> eventIds;

        boolean returnTrue = true;

        public SampleSubscirberFilter(List<String> eventIds){
            this.eventIds = eventIds;
        }

        @Override
        public boolean afterReceived(Target target, EventInfo eventInfo, Object result) {
            this.eventIds.add(eventInfo.getId());
            return returnTrue;
        }
    }

    class SampleEventListener implements EventListener {

        @Override
        public void onObserved(EventSourceBase source) {
            consumedEvent.set(true);
        }
    }
}