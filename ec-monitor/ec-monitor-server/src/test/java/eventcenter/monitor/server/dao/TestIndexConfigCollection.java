package eventcenter.monitor.server.dao;

import com.mongodb.client.MongoDatabase;
import eventcenter.monitor.server.model.IndexConfig;
import eventcenter.monitor.server.mongo.MongodbClientFactory;
import eventcenter.remote.utils.ExpiryMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * Created by liumingjian on 16/2/24.
 */
public class TestIndexConfigCollection {

    MongodbClientFactory factory;

    IndexConfigCollection collection;

    String group = "test";

    @Before
    public void setUp() throws Exception {
        factory = new MongodbClientFactory();
        factory.setDatabaseName("sample2");
        MongoDatabase db = factory.initConnections();
        collection = Mockito.spy(new IndexConfigCollection());
        collection.setDb(db);
        collection.db.drop();
    }

    @Test
    public void testSave1() throws Exception {
        IndexConfig config = new IndexConfig();
        config.setEventName("example");
        config.setArgsIndexes("[0].id");
        config.setGroup(group);
        collection.save(config);
        Assert.assertNotNull(config.getId());

        IndexConfig result = collection.queryById(config.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(config.getEventName(), result.getEventName());
        Assert.assertEquals(config.getArgsIndexes(), result.getArgsIndexes());
        Assert.assertEquals(group, result.getGroup());
    }

    @Test
    public void testSave2() throws Exception {
        IndexConfig config = new IndexConfig();
        config.setEventName("example");
        config.setResultIndexes("id,name,children.id");
        collection.save(config);
        Assert.assertNotNull(config.getId());

        IndexConfig result = collection.queryById(config.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(config.getEventName(), result.getEventName());
        Assert.assertEquals(config.getResultIndexes(), result.getResultIndexes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSave3() throws Exception {
        IndexConfig config = new IndexConfig();
        config.setEventName("example");
        collection.save(config);
    }

    @Test
    public void testSave4() throws Exception {
        IndexConfig config = new IndexConfig();
        config.setEventName("example1.*");
        config.setResultIndexes("id,name,children.id");
        config.setGroup(group);
        collection.save(config);
        Assert.assertNotNull(config.getId());

        IndexConfig result = collection.queryById(config.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(config.getEventName(), result.getEventName());
        Assert.assertEquals(config.getResultIndexes(), result.getResultIndexes());
        Assert.assertTrue(config.isWildcard());

        String eventName = "example1.test";
        IndexConfig ic = collection.queryByEventName(eventName, group);
        Assert.assertNotNull(ic);
        Assert.assertEquals(true, collection.cache.containsKey(collection.buildCacheKey(eventName, group)));

        ExpiryMap.ExpiryValue<IndexConfig> ev = collection.cache.get(collection.buildCacheKey(eventName, group));
        Date date = new Date(new Date().getTime() - (1200 * 1000));
        collection.cache.put(collection.buildCacheKey(eventName, group), ExpiryMap.ExpiryValue.build(date, 60000, ev.getValue()));

        ic = collection.queryByEventName(eventName, group);
        Assert.assertNotNull(ic);
        Assert.assertEquals(true, collection.cache.containsKey(collection.buildCacheKey(eventName, group)));
    }

    @Test
    public void testSave5() throws Exception {
        IndexConfig config = new IndexConfig();
        config.setEventName("example1.*");
        config.setResultIndexes("id,name,children.id");
        collection.save(config);
        Assert.assertNotNull(config.getId());

        IndexConfig result = collection.queryById(config.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(config.getEventName(), result.getEventName());
        Assert.assertEquals(config.getResultIndexes(), result.getResultIndexes());

        String eventName = "example2.test";
        IndexConfig ic = collection.queryByEventName(eventName, group);
        Assert.assertNull(ic);
    }

    @Test
    public void testDeleteById(){
        IndexConfig config = new IndexConfig();
        config.setEventName("example");
        config.setArgsIndexes("[0].id");
        config.setGroup(group);
        collection.save(config);
        Assert.assertNotNull(config.getId());

        Assert.assertEquals(1, collection.deleteById(config.getId()));
        Assert.assertNull(collection.queryById(config.getId()));
    }
}
