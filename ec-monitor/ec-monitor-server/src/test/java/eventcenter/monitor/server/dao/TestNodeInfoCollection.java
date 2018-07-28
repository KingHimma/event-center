package eventcenter.monitor.server.dao;

import com.mongodb.client.MongoDatabase;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.server.mongo.MongodbClientFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by liumingjian on 16/2/21.
 */
public class TestNodeInfoCollection {

    MongodbClientFactory factory;

    NodeInfoCollection collection;

    @Before
    public void setUp() throws Exception {
        factory = new MongodbClientFactory();
        factory.setDatabaseName("sample2");
        MongoDatabase db = factory.initConnections();
        collection = new NodeInfoCollection();
        collection.setDb(db);
    }

    @After
    public void tearDown() throws Exception {
        factory.destroy();

    }

    @Test
    public void testCreate(){
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(UUID.randomUUID().toString());
        nodeInfo.setGroup("test");
        nodeInfo.setName("test");
        nodeInfo.setHost("127.0.0.1");
        nodeInfo.setStat(1);
        nodeInfo.setStart(new Date());
        collection.save(nodeInfo);

        List<NodeInfo> list = collection.search(nodeInfo.getId(), null, null, null);
        Assert.assertEquals(1, list.size());
        NodeInfo result = list.get(0);
        Assert.assertEquals(nodeInfo.getId(), result.getId());
        Assert.assertEquals(nodeInfo.getGroup(), result.getGroup());
        Assert.assertEquals(nodeInfo.getName(), result.getName());
        Assert.assertEquals(nodeInfo.getHost(), result.getHost());
        Assert.assertEquals(nodeInfo.getStat(), result.getStat());

        nodeInfo.setGroup("jacky");
        nodeInfo.setName("jacky");
        nodeInfo.setHost("192.168.1.2");
        nodeInfo.setStat(2);

        collection.save(nodeInfo);

        list = collection.search(nodeInfo.getId(), null, null, null);
        Assert.assertEquals(1, list.size());
        result = list.get(0);
        Assert.assertEquals(nodeInfo.getId(), result.getId());
        Assert.assertEquals(nodeInfo.getGroup(), result.getGroup());
        Assert.assertEquals(nodeInfo.getName(), result.getName());
        Assert.assertEquals(nodeInfo.getHost(), result.getHost());
        Assert.assertEquals(nodeInfo.getStat(), result.getStat());
    }
}
