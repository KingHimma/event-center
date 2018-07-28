package eventcenter.remote.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试有效期Map
 * @author JackyLIU
 *
 */
public class TestExpiryMap {

	@Test
	public void test1() throws InterruptedException {
		ExpiryMap<String, String> map = new ExpiryMap<String, String>();
		map.setCheckInterval(1);
		map.startup();
		
		map.getMap().put("test1", ExpiryMap.ExpiryValue.build(1000, "sample"));
		
		Assert.assertEquals(true, map.getMap().containsKey("test1"));
		Thread.sleep(2050);
		Assert.assertEquals(false, map.getMap().containsKey("test1"));
		map.shutdown();
		
	}

}
