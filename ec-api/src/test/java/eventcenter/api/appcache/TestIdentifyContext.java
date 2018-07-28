package eventcenter.api.appcache;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by liumingjian on 16/2/18.
 */
public class TestIdentifyContext {

    @Test
    public void test() throws IOException {
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, new StringBuilder(".").append(File.separator).append("target").toString());
        String id = IdentifyContext.getId();
        Assert.assertTrue(id != null && !"".equals(id.trim()));
        System.out.println(id);
    }
}
