package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.common.URL;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RegistryDeduplicatorTest {


    RegistryDeduplicator deduplicator;

    @Before
    public void setUp() throws Exception {
        deduplicator = new RegistryDeduplicator();
    }

    @Ignore
    @Test
    public void isDeduplicate() throws InterruptedException {
        URL url1 = new URL("provider", "127.0.0.1", 2012, "/temp");
        URL url2 = new URL("provider", "127.0.0.1", 2012, "/temp");
        URL url3 = new URL("provider", "127.0.0.1", 2008, "/temp2");
        URL url4 = new URL("provider", "127.0.0.1", 2008, "/temp2");
        Assert.assertFalse(deduplicator.isDeduplicate(url1));
        Thread.sleep(100);
        Assert.assertTrue(deduplicator.isDeduplicate(url2));
        Thread.sleep(550);
        Assert.assertFalse(deduplicator.isDeduplicate(url1));
        Assert.assertFalse(deduplicator.isDeduplicate(url3));
        Thread.sleep(400);
        Assert.assertTrue(deduplicator.isDeduplicate(url1));
        Assert.assertTrue(deduplicator.isDeduplicate(url2));
        Assert.assertTrue(deduplicator.isDeduplicate(url3));
        Assert.assertTrue(deduplicator.isDeduplicate(url4));
    }
}