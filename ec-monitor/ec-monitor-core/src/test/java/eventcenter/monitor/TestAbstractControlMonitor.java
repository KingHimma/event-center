package eventcenter.monitor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by liumingjian on 16/2/16.
 */
public class TestAbstractControlMonitor {

    TestControlMonitor monitor;

    @Before
    public void setUp() throws Exception {
        monitor = new TestControlMonitor();
        monitor.setInfoStorage(Mockito.mock(InfoStorage.class));
    }

    @Test
    public void testLoadNodeInfo(){
        monitor.setNodeName("test");
        NodeInfo nodeInfo = monitor.loadNodeInfo();
        Assert.assertNotNull(nodeInfo);
        Assert.assertNotNull(nodeInfo.getStart());
        Assert.assertNotNull(nodeInfo.getId());
        Assert.assertNotNull(nodeInfo.getHost());
        Assert.assertNotNull(nodeInfo.getName());
        Assert.assertEquals(1, nodeInfo.getStat().intValue());
    }

    class TestControlMonitor extends AbstractControlMonitor {

        public TestControlMonitor() {

        }

        @Override
        protected InfoStorage loadInfoStorage() {
            return Mockito.mock(InfoStorage.class);
        }

        @Override
        public void shutdown() {

        }
    }
}
