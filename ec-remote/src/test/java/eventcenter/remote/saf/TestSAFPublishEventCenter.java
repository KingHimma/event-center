package eventcenter.remote.saf;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * Created by liumingjian on 16/2/18.
 */
public class TestSAFPublishEventCenter {

    SAFPublishEventCenter ec;

    @Before
    public void setUp() throws Exception {
        ec = new SAFPublishEventCenter();
    }

    @Test
    public void testStartUp() throws Exception {
        ec.startup();
        Assert.notNull(ec.eventForward);
        Assert.notNull(ec.getFireRemoteEventsPolicy());
        ec.shutdown();
    }


}
