package eventcenter.monitor.server.dao;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexModel;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.MonitorException;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.server.model.MonitorEventInfoModel;
import eventcenter.monitor.server.mongo.MongodbClientFactory;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by liumingjian on 16/2/19.
 */
public class TestEventInfoCollection {

    MongodbClientFactory factory;

    EventInfoCollection collection;

    NodeInfoCollection nodeInfoCollection;

    private String nodeId1 = "dd45050b-e51f-4f85-a765-51e60f952752";

    private String nodeId2 = "cb885451-14bd-4ae5-9f12-bbc9edb2efb1";

    @Before
    public void setUp() throws Exception {
        factory = new MongodbClientFactory();
        factory.setDatabaseName("sample2");
        MongoDatabase db = factory.initConnections();
        collection = new EventInfoCollection();
        collection.setDb(db);
        nodeInfoCollection = new NodeInfoCollection();
        nodeInfoCollection.setDb(db);
        collection.setNodeInfoCollection(nodeInfoCollection);
    }

    @After
    public void tearDown() throws Exception {
        factory.destroy();

    }

    @Test
    public void testInsertWithNodeId1() throws Exception {
        MonitorEventInfo info = new MonitorEventInfo();
        info.setNodeId(nodeId1);
        info.setTook(100L);
        info.setListenerClazz(this.getClass().getName());
        info.setSuccess(true);
        info.setException(new MonitorException("test"));
        info.setEventName("test");
        info.setEventId(UUID.randomUUID().toString());
        info.setEventArgs("\"test\"");
        info.setEventResult("sample");
        info.setMdcValue(UUID.randomUUID().toString());

        collection.insert(Arrays.asList(info));
    }

    @Test
    public void testInsertWithNodeId2() throws Exception {
        MonitorEventInfo info = new MonitorEventInfo();
        info.setNodeId(nodeId2);
        info.setTook(100L);
        info.setListenerClazz(this.getClass().getName());
        info.setSuccess(true);
        info.setEventName("test2");
        info.setEventId(UUID.randomUUID().toString());
        info.setEventArgs("\"test\"");
        info.setEventResult("sample");
        info.setMdcValue(UUID.randomUUID().toString());

        collection.insert(Arrays.asList(info));
    }

    @Test
    public void testSearch1(){
        final String eventId = "f30c3d39-1930-4899-8d45-88c5c2392fa2";
        List<MonitorEventInfoModel> list = collection.search(eventId, null, null, null, null, null, null, null, null, null).getList();
        Assert.assertEquals(1, list.size());
        MonitorEventInfoModel info = list.get(0);
        Assert.assertEquals(nodeId2, info.getNodeId());
        Assert.assertEquals(eventId, info.getEventId());
        Assert.assertEquals("jacky", info.getNodeName());
        Assert.assertEquals("jacky", info.getNodeGroup());
    }

    @Test
    public void testSearch2(){
        final String eventName = "test";
        List<MonitorEventInfoModel> list = collection.search(null, eventName, null, null, null, null, null, null, 2, null).getList();
        Assert.assertEquals(2, list.size());
        for(MonitorEventInfoModel model : list){
            Assert.assertEquals(eventName, model.getEventName());
        }
    }

    @Test
    public void testSearch3(){
        final String eventName = "test";
        List<MonitorEventInfoModel> list = collection.search(null, eventName, null, null, null, null, null, 1, 1, null).getList();
        Assert.assertEquals(1, list.size());
        String firstEventId = list.get(0).getEventId();

        list = collection.search(null, eventName, null, null, null, null, null, 2, 1, null).getList();
        Assert.assertEquals(1, list.size());
        String secondEventId = list.get(0).getEventId();
        Assert.assertNotEquals(firstEventId, secondEventId);
    }

    @Test
    public void testSearch4() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = format.parse("2016-02-23 09:00:00");
        List<MonitorEventInfoModel> list = collection.search(null, null, start, null, null, null, null, null, null, null).getList();
        Assert.assertEquals(true, list.size() > 0);
        for(MonitorEventInfoModel model : list){
            Assert.assertTrue(model.getEventCreate().after(start));
        }
    }

    @Test
    public void testSearch5() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date end = format.parse("2016-02-23 09:00:00");
        List<MonitorEventInfoModel> list = collection.search(null, null, null, end, null, null, null, null, null, null).getList();
        Assert.assertEquals(true, list.size() > 0);
        for(MonitorEventInfoModel model : list){
            Assert.assertTrue(model.getEventCreate().before(end));
        }
    }

    @Test
    public void testSearch6() throws ParseException {
        final String className = this.getClass().getName();
        List<MonitorEventInfoModel> list = collection.search(null, null, null, null, className, null, null, null, null, null).getList();
        Assert.assertEquals(true, list.size() > 0);
        for(MonitorEventInfoModel model : list){
            Assert.assertEquals(className, model.getListenerClazz());
        }

        list = collection.search(null, null, null, null, className + "0", null, null, null, null, null).getList();
        Assert.assertEquals(true, list.size() == 0);
    }

    @Test
    public void testSearch7(){
        final String mdcValue = "24fe54af-cded-4af0-b0b5-e1851fbf2b65";
        List<MonitorEventInfoModel> list = collection.search(null, null, null, null, null, mdcValue, null, null, null, null).getList();
        Assert.assertEquals(1, list.size());
        MonitorEventInfoModel model = list.get(0);
        Assert.assertEquals(mdcValue, model.getMdcValue());
    }

    @Test
    public void testSearch8(){
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(nodeId1);
        List<MonitorEventInfoModel> list = collection.search(null, null, null, null, null, null, null, null, null, nodeInfo).getList();
        Assert.assertTrue(list.size() > 0);
        for(MonitorEventInfoModel model : list){
            Assert.assertEquals(nodeInfo.getId(), model.getNodeId());
        }
    }

    @Test
    public void testSearch9(){
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setName("jacky");
        List<MonitorEventInfoModel> list = collection.search(null, null, null, null, null, null, null, null, null, nodeInfo).getList();
        Assert.assertTrue(list.size() > 0);
        for(MonitorEventInfoModel model : list){
            Assert.assertEquals(nodeInfo.getName(), model.getNodeName());
        }
    }

    @Test
    public void testSearch10() throws ParseException {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setName("jacky");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = format.parse("2016-02-23 09:00:00");
        List<MonitorEventInfoModel> list = collection.search(null, null, start, null, null, null, null, null, 2, nodeInfo).getList();
        Assert.assertTrue(list.size() > 0 && list.size() <= 2);
        for(MonitorEventInfoModel model : list){
            Assert.assertEquals(nodeInfo.getName(), model.getNodeName());
            Assert.assertTrue(model.getEventCreate().after(start));
        }
    }

    @Test
    public void testCreateIndex(){
        collection.getDb().getCollection("sample").createIndexes(Arrays.asList(new IndexModel(new Document().append("nodeId",1)),
                new IndexModel(new Document().append("listenerClass", 1)),
                new IndexModel(new Document().append("eventId", 1)),
                new IndexModel(new Document().append("fromNodeId", 1)),
                new IndexModel(new Document().append("eventName", 1)),
                new IndexModel(new Document().append("eventCreated", 1)),
                new IndexModel(new Document().append("mdcValue", 1)),
                new IndexModel(new Document().append("args", 1)),
                new IndexModel(new Document().append("result", 1))));
    }
}
