package eventcenter.monitor.server.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.IndexModel;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.server.model.IndexConfig;
import eventcenter.monitor.server.model.MonitorEventInfoModel;
import eventcenter.monitor.server.model.MonitorEventInfoModels;
import eventcenter.monitor.server.utils.JsonRuleEngine;
import eventcenter.monitor.server.model.MonitorEventInfoModel;
import eventcenter.monitor.server.utils.JsonRuleEngine;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by liumingjian on 16/2/19.
 */
@Repository
public class EventInfoCollection extends MongodbCollection{

    private final String collectionName = "eventInfos";

    @Resource
    private NodeInfoCollection nodeInfoCollection;

    @Resource
    private IndexConfigCollection indexConfigCollection;

    private JsonRuleEngine jsonRuleEngine = new JsonRuleEngine();

    public NodeInfoCollection getNodeInfoCollection() {
        return nodeInfoCollection;
    }

    public void setNodeInfoCollection(NodeInfoCollection nodeInfoCollection) {
        this.nodeInfoCollection = nodeInfoCollection;
    }

    public IndexConfigCollection getIndexConfigCollection() {
        return indexConfigCollection;
    }

    public void setIndexConfigCollection(IndexConfigCollection indexConfigCollection) {
        this.indexConfigCollection = indexConfigCollection;
    }

    public void insert(List<MonitorEventInfo> infos){
        List<Document> docs = new ArrayList<Document>(infos.size() + 1);
        final Map<String, IndexConfig> indexConfigCache = new HashMap<String, IndexConfig>();
        for(MonitorEventInfo info : infos){
            docs.add(toDocument(info, indexConfigCache));
        }
        insertMany(docs);
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @PostConstruct
    @Override
    public void createIndexes() {
        try {
            getCollection().createIndexes(Arrays.asList(new IndexModel(new Document().append("nodeId", 1)),
                    new IndexModel(new Document().append("listenerClass", 1)),
                    new IndexModel(new Document().append("eventId", 1)),
                    new IndexModel(new Document().append("fromNodeId", 1)),
                    new IndexModel(new Document().append("eventName", 1)),
                    new IndexModel(new Document().append("eventCreated", 1)),
                    new IndexModel(new Document().append("mdcValue", 1)),
                    new IndexModel(new Document().append("custom", "text"))));
        }catch(Exception e){
            logger.error("create indexes error:" + e.getMessage(), e);
        }
    }

    public MonitorEventInfoModels search(String eventId, String eventName, Date start, Date end, String listenerClass, String mdcValue, String content, Integer pageNo, Integer pageSize, NodeInfo nodeInfo){
        List<NodeInfo> nodeInfos = null;
        if(null != nodeInfo){
            nodeInfos = nodeInfoCollection.search(nodeInfo);
        }

        final Map<String, NodeInfo> filterNodeMap = new HashMap<String, NodeInfo>();
        Set<String> filterNodeIds = null;
        if(null != nodeInfos && nodeInfos.size() > 0){
            filterNodeMap.putAll(toMap(nodeInfos));
            filterNodeIds = filterNodeMap.keySet();
        }
        return MonitorEventInfoModels.build((int)searchCount(eventId, eventName, start, end, listenerClass, mdcValue, content, nodeInfos, filterNodeMap, filterNodeIds),
                _search(eventId, eventName, start, end, listenerClass, mdcValue, content, pageNo, pageSize, nodeInfos, filterNodeMap, filterNodeIds));
    }

    public MonitorEventInfoModel queryById(String id){
        if(isEmpty(id))
            throw new IllegalArgumentException("please input id arguments");
        final List<MonitorEventInfoModel> result = new ArrayList<MonitorEventInfoModel>(2);
        find(new Document("_id", new ObjectId(id))).forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                String nodeId = document.getString("nodeId");
                NodeInfo nodeInfo = null;
                if(isNotEmpty(nodeId)){
                    List<NodeInfo> nodes = nodeInfoCollection.queryByIds(Arrays.asList(nodeId));
                    if(nodes.size() > 0){
                        nodeInfo = nodes.get(0);
                    }
                }
                result.add(toMonitorEventInfo(document,nodeInfo));
            }
        });
        if(result.size() == 0)
            return null;
        return result.get(0);
    }

    /**
     * 数据清理
     * @param keepdays 保留多少天内的数据
     */
    public long houseKeeping(int keepdays){
        Date begin = new Date(new Date().getTime() - (keepdays * 24 * 3600 * 1000));
        return deleteMany(new Document("created",new Document("$lte", begin))).getDeletedCount();
    }

    protected List<MonitorEventInfoModel> _search(String eventId, String eventName, Date start, Date end, String listenerClass, String mdcValue, String content, Integer pageNo, Integer pageSize, List<NodeInfo> nodeInfos, final Map<String, NodeInfo> filterNodeMap, Set<String> filterNodeIds){
        Document filter = buildCondition(eventId, eventName, start, end, listenerClass, mdcValue, content, nodeInfos, filterNodeMap, filterNodeIds);
        if(pageSize == null)
            pageSize = 20;
        FindIterable<Document> results = find(filter).limit(pageSize).projection(new Document().append("args",0).append("result",0));
        if(null != pageNo){
            results.skip((pageNo - 1)*pageSize);
        }
        final List<MonitorEventInfoModel> list = new ArrayList<MonitorEventInfoModel>(pageSize + 1);
        results.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                NodeInfo nodeInfo = filterNodeMap.get(document.getString("nodeId"));
                if(null == nodeInfo){
                    List<NodeInfo> infos = getNodeInfoCollection().queryByIds(Arrays.asList(document.getString("nodeId")));
                    if(infos.size() > 0){
                        nodeInfo = infos.get(0);
                        filterNodeMap.put(nodeInfo.getId(), nodeInfo);
                    }
                }
                list.add(toMonitorEventInfo(document,nodeInfo));
            }
        });
        return list;
    }

    private long searchCount(String eventId, String eventName, Date start, Date end, String listenerClass, String mdcValue, String content, List<NodeInfo> nodeInfos, final Map<String, NodeInfo> filterNodeMap, Set<String> filterNodeIds){
        Document filter = buildCondition(eventId, eventName, start, end, listenerClass, mdcValue, content, nodeInfos, filterNodeMap, filterNodeIds);
        return getCollection().count(filter);
    }

    private Document buildCondition(String eventId, String eventName, Date start, Date end, String listenerClass, String mdcValue, String content, List<NodeInfo> nodeInfos, final Map<String, NodeInfo> filterNodeMap, Set<String> filterNodeIds){
        Document filter = new Document();
        if(isNotEmpty(eventId)){
            filter.append("eventId", eventId);
        }
        if(isNotEmpty(eventName)){
            filter.append("eventName", eventName);
        }
        if(isNotEmpty(listenerClass)){
            filter.append("listenerClass", listenerClass);
        }
        if(isNotEmpty(mdcValue)){
            filter.append("mdcValue", mdcValue);
        }
        if(start != null){
            filter.append("eventCreated", new Document("$gte", start));
        }
        if(end != null){
            filter.append("eventCreated", new Document("$lte", end));
        }
        if(null != filterNodeIds){
            List<Document> nodeIdDocs = new ArrayList<Document>(filterNodeIds.size() + 1);
            for(String nodeId : filterNodeIds){
                nodeIdDocs.add(new Document("nodeId", nodeId));
            }
            filter.append("$or", nodeIdDocs);
        }
        if(isNotEmpty(content)){
            filter.append("$text", new Document("$search", content));
        }
        return filter;
    }

    private Map<String, NodeInfo> toMap(List<NodeInfo> infos){
        Map<String, NodeInfo> map = new HashMap<String, NodeInfo>();
        for(NodeInfo info : infos){
            map.put(info.getId(), info);
        }
        return map;
    }

    private MonitorEventInfoModel toMonitorEventInfo(Document doc, NodeInfo nodeInfo){
        MonitorEventInfoModel info = new MonitorEventInfoModel();
        info.setId(doc.getObjectId("_id").toString());
        info.setNodeId(doc.getString("nodeId"));
        info.setEventCreate(doc.getDate("eventCreated"));
        info.setException(doc.getString("exception"));
        info.setListenerClazz(doc.getString("listenerClass"));
        info.setFromNodeId(doc.getString("fromNodeId"));
        info.setSuccess(doc.getBoolean("success"));
        info.setTook(doc.getLong("took"));
        info.setEventArgs(doc.getString("args"));
        info.setEventResult(doc.getString("result"));
        info.setEventId(doc.getString("eventId"));
        info.setEventName(doc.getString("eventName"));
        info.setMdcValue(doc.getString("mdcValue"));
        info.setEventConsumed(doc.getDate("eventConsumed"));
        if(null != nodeInfo){
            info.setNodeName(nodeInfo.getName());
            info.setNodeGroup(nodeInfo.getGroup());
            info.setNodeHost(nodeInfo.getHost());
        }
        return info;
    }

    private Document toDocument(MonitorEventInfo info, Map<String, IndexConfig> indexConfigCache){
        String excp = "";
        boolean success = true;
        if(info.getException() != null){
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            try {
                info.getException().printStackTrace(writer);
            }finally {
                writer.close();
            }
            excp = sw.toString();
            success = false;
        }

        Document doc = new Document()
                .append("nodeId", info.getNodeId())
                .append("took", info.getTook())
                .append("listenerClass", info.getListenerClazz())
                .append("exception",excp)
                .append("success",success)
                .append("eventId",info.getEventId())
                .append("fromNodeId",info.getFromNodeId()==null?"":info.getFromNodeId())
                .append("eventName",info.getEventName())
                .append("eventCreated", info.getCreated())
                .append("eventConsumed", info.getConsumed())
                .append("created", new Date())
                .append("mdcValue", info.getMdcValue())
                .append("args", info.getEventArgs())
                .append("result", info.getEventResult());
        String customIndex = createCustomIndex(info, indexConfigCache);
        if(isEmpty(customIndex))
            return doc;
        return doc.append("custom", customIndex);
    }

    private IndexConfig queryIndexConfig(MonitorEventInfo info, Map<String, IndexConfig> indexConfigCache){
        String key = new StringBuilder(info.getNodeGroup()).append("_").append(info.getEventName()).toString();
        if(indexConfigCache.containsKey(key))
            return indexConfigCache.get(key);

        IndexConfig indexConfig = indexConfigCollection.queryByEventName(info.getEventName(), info.getNodeGroup());
        try{
            if(null == indexConfig){
                return null;
            }

            if(isEmpty(info.getEventArgs()) && isEmpty(info.getEventResult())){
                indexConfig = null;
                return null;
            }

            return indexConfig;
        }finally {
            indexConfigCache.put(key, indexConfig);
        }
    }

    private String createCustomIndex(MonitorEventInfo info, Map<String, IndexConfig> indexConfigCache){
        IndexConfig indexConfig = queryIndexConfig(info, indexConfigCache);
        if(null == indexConfig)
            return "";

        StringBuilder sb = new StringBuilder();
        if(isNotEmpty(info.getEventArgs()) && isNotEmpty(indexConfig.getArgsIndexes())){
            _createCustomIndex(sb, info.getEventArgs(), indexConfig.getArgsIndexes());
        }
        if (isNotEmpty(info.getEventResult()) && isNotEmpty(indexConfig.getResultIndexes())){
            _createCustomIndex(sb, info.getEventResult(), indexConfig.getResultIndexes());
        }

        return sb.toString();
    }

    private void _createCustomIndex(StringBuilder sb, String json, String rule){
        if(isEmpty(json) || isEmpty(rule) || sb == null)
            return ;
        if(sb.length() > 0){
            sb.append(jsonRuleEngine.getSeparatorChar());
        }
        try {
            sb.append(jsonRuleEngine.resolve((JSON) JSON.parse(json), rule));
        }catch(Exception e){
            logger.error("resolve json error:" + e.getMessage(), e);
        }
    }

    private Document fromJSONObject(JSONObject root){
        return new Document(root);
    }

    private String toJSON(Object obj){
        try {
            return JSON.toJSONString(obj, SerializerFeature.UseISO8601DateFormat, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteClassName);
        }catch(Exception e){
            logger.error("parse arg to json error:" + e.getMessage(), e);
            return "";
        }
    }
}
