package eventcenter.monitor.client.leveldb;

import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by liumingjian on 16/2/15.
 */
public class TestLeveldbInfoStorage {

    LeveldbInfoStorage storage;

    @Before
    public void setUp(){
        storage = new LeveldbInfoStorage();
        storage.setDirPath(new File(new StringBuilder(".").append(File.separator).append("target").append(File.separator).append(".ecmonitor").toString()));
    }

    @Test
    public void testOpenAndClose() throws Exception {
        storage.open();
        storage.close();
    }

    @Test
    public void testPushAndPop1() throws Exception {
        storage.open();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        info = storage.popEventInfo();
        Assert.assertEquals(100L, info.getTook().longValue());
        storage.close();
    }

    @Test
    public void testPushAndPop2() throws Exception {
        storage.open();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        info = new MonitorEventInfo();
        info.setTook(101L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        info = storage.popEventInfo();
        Assert.assertEquals(100L, info.getTook().longValue());
        info = storage.popEventInfo();
        Assert.assertEquals(101L, info.getTook().longValue());
        storage.close();
    }

    @Test
    public void testPushAndPop3() throws Exception {
        storage.open();
        MonitorEventInfo info = new MonitorEventInfo();
        info.setTook(100L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        info = new MonitorEventInfo();
        info.setTook(101L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        info = new MonitorEventInfo();
        info.setTook(102L);
        info.setSuccess(true);
        storage.pushEventInfo(info);
        List<MonitorEventInfo> infos = storage.popEventInfos(10);
        Assert.assertEquals(100L, infos.get(0).getTook().longValue());
        Assert.assertEquals(101L, infos.get(1).getTook().longValue());
        Assert.assertEquals(102L, infos.get(2).getTook().longValue());
        Assert.assertNull(storage.popEventInfo());
        storage.close();
    }

    @Test
    public void testSaveNodeInfo() throws Exception {
        storage.open();
        NodeInfo ni = new NodeInfo();
        ni.setName("localhost_test");
        ni.setId(UUID.randomUUID().toString());
        storage.saveNodeInfo(ni);
        NodeInfo nodeInfo = storage.queryNodeInfo();
        Assert.assertNotNull(nodeInfo);
        Assert.assertEquals("localhost_test", nodeInfo.getName());
        nodeInfo.setName("test");
        storage.saveNodeInfo(nodeInfo);
        nodeInfo = storage.queryNodeInfo();
        Assert.assertEquals("test", nodeInfo.getName());
        storage.close();
    }
}
