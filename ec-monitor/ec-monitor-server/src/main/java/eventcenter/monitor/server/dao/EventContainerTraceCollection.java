package eventcenter.monitor.server.dao;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.IndexModel;
import eventcenter.monitor.server.model.ContainerTrace;
import eventcenter.monitor.server.model.ContainerTraces;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Date;

/**
 * 事件容器的运行数据
 * Created by liumingjian on 16/5/31.
 */
@Repository
public class EventContainerTraceCollection extends MongodbCollection {

    private final String collectionName = "containerTrace";

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void createIndexes() {
        try {
            getCollection().createIndexes(Arrays.asList(new IndexModel(new Document().append("nodeId", 1)),
                    new IndexModel(new Document().append("created", 1))));
        }catch(Exception e){
            logger.error("create indexes error:" + e.getMessage(), e);
        }
    }

    public void insert(ContainerTrace trace){
        if(null == trace.getCreated())
            trace.setCreated(new Date());
        insertOne(toDocument(trace));
    }

    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @param start
     * @param end
     * @return
     */
    public ContainerTraces searchWithPage(String nodeId, Integer pageNo, Integer pageSize, Date start, Date end){
        if(null == pageNo)
            pageNo = 1;
        if(null == pageSize)
            pageSize = 100;

        Document filter = buildCondition(nodeId, start, end);
        FindIterable<Document> results = find(filter).skip((pageNo - 1) * pageSize).limit(pageSize);
        final ContainerTraces traces = new ContainerTraces();
        results.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                traces.getList().add(toContainerTrace(document));
            }
        });
        traces.setTotal((int)getCount(nodeId, start, end));
        return traces;
    }

    private long getCount(String nodeId, Date start, Date end){
        Document filter = buildCondition(nodeId, start, end);
        return getCollection().count(filter);
    }

    private Document buildCondition(String nodeId, Date start, Date end){
        Document filter = new Document();
        if(isNotEmpty(nodeId))
            filter.append("nodeId", nodeId);
        if(null != start)
            filter.append("created", new Document("$gte", start));
        if(null != end)
            filter.append("created", new Document("$lte", end));
        return filter;
    }

    /**
     * 数据清理
     * @param keepdays 保留多少天内的数据
     */
    public long houseKeeping(int keepdays){
        Date begin = new Date(new Date().getTime() - (keepdays * 24 * 3600 * 1000));
        return deleteMany(new Document("created",new Document("$lte", begin))).getDeletedCount();
    }

    private Document toDocument(ContainerTrace trace){
        return new Document()
                .append("nodeId", trace.getNodeId())
                .append("queueSize", trace.getQueueSize())
                .append("liveThread", trace.getCountOfLiveThread())
                .append("queueBuffer", trace.getCountOfQueueBuffer())
                .append("created", trace.getCreated());
    }

    private ContainerTrace toContainerTrace(Document doc){
        ContainerTrace trace = new ContainerTrace();
        trace.setNodeId(doc.getString("nodeId"));
        trace.setQueueSize(doc.getInteger("queueSize"));
        trace.setCountOfQueueBuffer(doc.getInteger("queueBuffer"));
        trace.setCountOfLiveThread(doc.getInteger("liveThread"));
        trace.setCreated(doc.getDate("created"));
        return trace;
    }
}
