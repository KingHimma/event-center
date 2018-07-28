package eventcenter.monitor.client;

import eventcenter.api.AbstractEventCenter;
import eventcenter.api.EventContainer;
import eventcenter.monitor.MonitorEventInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liumingjian on 2017/2/15.
 */
public class LogControlMonitorTest {

    LogControlMonitor monitor;

    public void LogControlMonitorTest(){
        org.apache.log4j.xml.DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));
    }

    @Before
    public void setUp() throws Exception {
        monitor = new LogControlMonitor();
        monitor.setHeartbeatInterval(100);
        AbstractEventCenter eventCenter = Mockito.mock(AbstractEventCenter.class);
        EventContainer container = Mockito.mock(EventContainer.class);
        Mockito.when(eventCenter.getAsyncContainer()).thenReturn(container);
        Mockito.when(container.countOfLiveThread()).thenReturn(1);
        Mockito.when(container.countOfMaxConcurrent()).thenReturn(1);
        Mockito.when(container.countOfQueueBuffer()).thenReturn(1);
        Mockito.when(container.queueSize()).thenReturn(1);
        monitor.setEventCenter(eventCenter);
        monitor.startup();
    }

    @After
    public void tearDown() throws Exception {
        monitor.shutdown();
    }

    @Test
    public void test1() throws Exception {
        Thread.sleep(2000);
        MonitorEventInfo info = new MonitorEventInfo();
        info.setConsumed(new Date());
        info.setCreated(new Date());
        info.setDelay(10L);
        info.setEventArgs("test");
        info.setEventId(UUID.randomUUID().toString());
        info.setEventName("test");
        info.setEventResult("test");
        info.setFromNodeId(UUID.randomUUID().toString());
        info.setListenerClazz(this.getClass().getName());
        info.setNodeGroup("test");
        info.setTook(10L);
        monitor.saveEventInfo(info);
        Thread.sleep(100);
    }
}