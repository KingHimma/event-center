package eventcenter.monitor.server.dao;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import eventcenter.monitor.NodeInfo;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * 事件中心节点信息
 * Created by liumingjian on 16/2/21.
 */
@Repository
public class NodeInfoCollection extends MongodbCollection {
    private final String collectionName = "nodeInfo";

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * 保存节点信息
     * @param nodeInfo
     */
    public void save(NodeInfo nodeInfo){
        if(nodeInfo == null)
            throw new IllegalArgumentException("please set nodeInfo");
        if(nodeInfo.getId() == null)
            throw new IllegalArgumentException("please set nodeInfo.id arguments");

        Document doc = toDocument(nodeInfo);
        UpdateResult result = getCollection().replaceOne(eq("_id", nodeInfo.getId()), doc);
        if(result.getMatchedCount() == 0){
            getCollection().insertOne(doc);
            logger.info(new StringBuilder("insert a new node info:").append(doc.toJson()));
        }
    }

    public List<NodeInfo> search(String id, String group, String name, String host){
        Document filter = new Document();
        if(isNotEmpty(id)){
            filter.append("_id", id);
        }
        if(isNotEmpty(group)){
            filter.append("group", group);
        }
        if(isNotEmpty(name)){
            filter.append("name", name);
        }
        if(isNotEmpty(host)){
            filter.append("host", host);
        }
        FindIterable<Document> results = find(filter);
        final List<NodeInfo> list = new ArrayList<NodeInfo>();
        results.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                list.add(toNodeInfo(document));
            }
        });
        return list;
    }

    public List<NodeInfo> search(NodeInfo nodeInfo){
        if(null == nodeInfo)
            nodeInfo = new NodeInfo();
        return search(nodeInfo.getId(), nodeInfo.getGroup(), nodeInfo.getName(), nodeInfo.getHost());
    }

    public List<NodeInfo> queryByIds(List<String> ids){
        if(null == ids || ids.size() == 0)
            throw new IllegalArgumentException("set ids arguments");

        List<Document> conditions = new ArrayList<Document>(ids.size() + 1);
        for(String id : ids){
            conditions.add(new Document("_id", id));
        }
        FindIterable<Document> results = find(new Document("$or", conditions));
        final List<NodeInfo> list = new ArrayList<NodeInfo>(ids.size() + 1);
        results.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                list.add(toNodeInfo(document));
            }
        });
        return list;
    }

    @Override
    public void createIndexes() {

    }

    protected NodeInfo toNodeInfo(Document doc){
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setName(doc.getString("name"));
        nodeInfo.setStart(doc.getDate("lastStart"));
        nodeInfo.setStat(doc.getInteger("stat"));
        nodeInfo.setGroup(doc.getString("group"));
        nodeInfo.setHost(doc.getString("host"));
        nodeInfo.setId(doc.getString("_id"));
        return nodeInfo;
    }

    protected Document toDocument(NodeInfo nodeInfo){
        return new Document().append("_id", nodeInfo.getId())
                .append("group", nodeInfo.getGroup())
                .append("host", nodeInfo.getHost()==null?"":nodeInfo.getHost())
                .append("name", nodeInfo.getName()==null?"":nodeInfo.getName())
                .append("lastStart", nodeInfo.getStart())
                .append("stat", nodeInfo.getStat()==null?0:nodeInfo.getStat())
                .append("modified", new Date());
    }
}
