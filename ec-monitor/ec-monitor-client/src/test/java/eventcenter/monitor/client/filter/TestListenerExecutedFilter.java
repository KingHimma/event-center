package eventcenter.monitor.client.filter;

import eventcenter.api.CommonEventSource;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.appcache.AppDataContext;
import eventcenter.api.appcache.IdentifyContext;
import eventcenter.monitor.InfoForward;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.client.DefaultControlMonitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by liumingjian on 16/2/18.
 */
public class TestListenerExecutedFilter {

    ListenerReceipt receipt;

    ListenerExecutedFilter filter;

    DefaultControlMonitor monitor;

    final String path = "." + File.separator + "target";

    public TestListenerExecutedFilter(){
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, path);
    }

    @Before
    public void setUp() throws Exception {
        monitor = Mockito.spy(new DefaultControlMonitor());
        monitor.setInfoForward(Mockito.mock(InfoForward.class));
        monitor.setDirPath(path);
        monitor.setNodeName("test");
        monitor.startup();

        filter = new ListenerExecutedFilter();
        filter.setControlMonitor(monitor);
        receipt = new ListenerReceipt();
        receipt.setSuccess(true);
        receipt.setTook(100L);
        receipt.setEvt(new CommonEventSource(this, UUID.randomUUID().toString(), "test", null, null, null));
    }

    @After
    public void tearDown() throws Exception {
        monitor.shutdown();

    }

    @Test
    public void testAfter() throws IOException {
        filter.after(receipt);
        MonitorEventInfo monitorEventInfo = monitor.getInfoStorage().popEventInfo();
        Assert.assertNotNull(monitorEventInfo);
        String id = IdentifyContext.getId();
        Assert.assertEquals(id, monitorEventInfo.getNodeId());
        Assert.assertEquals("test", monitorEventInfo.getEventName());
        Assert.assertEquals(100, monitorEventInfo.getTook().intValue());
    }
}
