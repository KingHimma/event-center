package eventcenter.api.appcache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by liumingjian on 15/12/13.
 */
public class TestAppDataContext {

    String userHome = System.getProperty("user.home");

    public TestAppDataContext(){

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("user.home", userHome);
    }

    @Test
    public void testGetDataPath(){
        AppDataContext ctx = new AppDataContext();
        Assert.assertEquals(System.getProperty("user.home") + File.separator + ".ecapp" + File.separator + "df", ctx.getDataPath());
        System.setProperty("user.home", "");
        Assert.assertEquals(System.getProperty("user.dir") + File.separator + ".ecapp" + File.separator + "df", ctx.getDataPath());
        System.setProperty("user.home", System.getProperty("user.home") + File.separator);
        Assert.assertEquals(System.getProperty("user.home") + ".ecapp" + File.separator + "df", ctx.getDataPath());
    }

    @Test
    public void testInit() throws IOException {
        AppDataContext ctx = new AppDataContext();
        ctx.init();
        Assert.assertEquals(System.getProperty("user.home") + File.separator + ".ecapp" + File.separator + "df", ctx.path.getPath());
    }

    @Test
    public void testCreateData() throws IOException {
        AppDataContext ctx = new AppDataContext();
        ctx.init();
        Properties props = ctx.createData("sample");
        Assert.assertNotNull(props);
    }

    @Test
    public void testSaveData() throws IOException {
        AppDataContext ctx = new AppDataContext();
        ctx.init();
        Properties props = ctx.createData("sample");
        props.put("test","jacky");
        ctx.saveData("sample", props);

        props = ctx.createData("sample");
        Assert.assertEquals("jacky", props.get("test"));
    }

    @Test
    public void testChangeAppName(){
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_APPDATA_NAME, "test");
        AppDataContext ctx = new AppDataContext();
        Assert.assertEquals(System.getProperty("user.home") + File.separator + ".ecapp" + File.separator + "test", ctx.getDataPath());

    }
}