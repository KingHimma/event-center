package eventcenter.monitor.client;

import eventcenter.api.CommonEventSource;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.appcache.AppDataContext;
import eventcenter.monitor.AbstractMonitorDataCodec;
import eventcenter.monitor.InfoForward;
import eventcenter.monitor.InfoStorage;
import eventcenter.monitor.MonitorEventInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liumingjian on 16/2/16.
 */
public class TestDefaultControlMonitor {

    DefaultControlMonitor monitor;

    public TestDefaultControlMonitor(){
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
    }

    @Before
    public void setUp() throws Exception {
        monitor = Mockito.spy(new DefaultControlMonitor());
        monitor.setInfoForward(Mockito.mock(InfoForward.class));
        monitor.setDirPath(new StringBuilder(".").append(File.separator).append("target").toString());
    }

    /*@Test
    public void testStartAndShutdown(){
        monitor.startup();
        Assert.assertNotNull(monitor.getInfoStorage());
        monitor.shutdown();
    }*/

    @Test
    public void testSaveMonitorEventInfo() throws InterruptedException {
        monitor.startup();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        monitor.saveEventInfo(info);
        Thread.sleep(150L);
        Mockito.verify(monitor.getInfoForward(), Mockito.atLeastOnce()).forwardEventInfo(Mockito.anyList());
        monitor.shutdown();
    }

    @Test
    public void testSaveMonitorEventInfo2() throws InterruptedException {
        monitor.setCheckInterval(1000L);
        monitor.startup();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        monitor.saveEventInfo(info);
        Thread.sleep(150L);
        Mockito.verify(monitor.getInfoForward(), Mockito.atLeast(0)).forwardEventInfo(Mockito.anyList());
        Thread.sleep(1000L);
        Mockito.verify(monitor.getInfoForward(), Mockito.atLeast(1)).forwardEventInfo(Mockito.anyList());
        monitor.shutdown();
    }

    /*@Test
    public void testSaveMonitorEventInfo3() throws InterruptedException {
        monitor.setCheckInterval(1000L);
        Mockito.doThrow(MonitorException.class).when(monitor.getInfoForward()).forwardEventInfo(Mockito.anyList());
        monitor.startup();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        monitor.saveEventInfo(info);
        Thread.sleep(100L);
        Assert.assertEquals(1, monitor.watchDog.lastFailInfos.size());
        monitor.shutdown();
    }*/

    @Test
    public void testSaveListenerReceipt1() throws Exception {
        monitor.setCheckInterval(1000L);
        monitor.startup();
        ListenerReceipt receipt = new ListenerReceipt();
        receipt.setTook(100L);
        receipt.setSuccess(true);
        EventArg eventArg = new EventArg();
        eventArg.setId(1L);
        eventArg.setName("test");
        CommonEventSource source = new CommonEventSource(this, UUID.randomUUID().toString(), "test", new Object[]{eventArg}, eventArg, "123");
        receipt.setEvt(source);

        monitor.saveListenerReceipt(receipt);
        monitor.shutdown();
    }

    @Test
    public void testCodecObject() throws Exception {
        SampleMonitorDataCodec codec = new SampleMonitorDataCodec();
        monitor.setMonitorDataCodec(codec);
        EventArg eventArg = new EventArg();
        eventArg.setId(1L);
        eventArg.setName("test");
        Map<String, Object> data = (Map<String, Object>)monitor.codecObject("test", eventArg);
        Assert.assertEquals("test", data.get("name"));
    }

    @Test
    public void testWatchDog() throws Throwable {
        DefaultControlMonitor.WatchDog watchDog =  Mockito.spy(monitor.new WatchDog(monitor.getInfoForward()));
        monitor.setInfoStorage(Mockito.mock(InfoStorage.class));
        MonitorEventInfo eventInfo = new MonitorEventInfo();
        eventInfo.setEventId(UUID.randomUUID().toString());
        List<MonitorEventInfo> eventInfos = Arrays.asList(eventInfo);
        Mockito.when(monitor.getInfoStorage().popEventInfos(monitor.getBatchForwardSize())).thenReturn(eventInfos);
        Mockito.doThrow(RuntimeException.class).when(watchDog).forward(eventInfos);
        watchDog.run();
    }

    class SampleMonitorDataCodec extends AbstractMonitorDataCodec {
        @Override
        protected Map<String, Object> codecElement(Object data) {
            if(data instanceof EventArg){
                return new Builder().append("name", ((EventArg) data).getName()).build();
            }
            return null;
        }
    }

    public static class EventArg{
        private String name;

        private Long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
