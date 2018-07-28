package eventcenter.monitor.elasticsearch;

import eventcenter.monitor.InfoForward;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import eventcenter.remote.utils.StringHelper;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 推送到ElasticSearch的推送服务
 * Created by liumingjian on 2016/10/24.
 */
public class ElasticSearchInfoForward implements InfoForward {

    public static final String INDEX_NODE = "ec-node";

    public static final String INDEX_EVENT = "ec-event";

    public static final String INDEX_SEND = "ec-send";

    public static final String INDEX_RECEIVED = "ec-received";

    public static final Integer MINMUM_PUSH_NODE_INFO_INTERVAL = 30;

    protected static final String MAPPING_NODE_STRING = "{\n" +
            "        \"node\" : {\n" +
            "            \"properties\" : {\n" +
            "                \"start\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"timestamp\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"id\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"group\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"name\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"host\" : {\"type\" : \"ip\", \"index\" : \"not_analyzed\"},\n" +
            "                \"stat\" : {\"type\" : \"long\"},\n" +
            "                \"queueSize\" : {\"type\" : \"long\"},\n" +
            "                \"countOfLiveThread\" : {\"type\" : \"integer\"},\n" +
            "                \"countOfQueueBuffer\" : {\"type\" : \"long\"}\n" +
            "            }\n" +
            "        }\n" +
            "    }";

    protected static final String MAPPING_EVENT_STRING = "{\n" +
            "    \t\"event\" : {\n" +
            "            \"properties\" : {\n" +
            "                \"created\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"start\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"consumed\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"nodeId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeGroup\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeHost\" : {\"type\" : \"ip\", \"index\" : \"not_analyzed\"},\n" +
            "                \"fromNodeId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"exception\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"listenerClazz\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"exception\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventArgs\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventResult\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventName\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"mdcValue\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"took\" : {\"type\" : \"long\"},\n" +
            "                \"delay\" : {\"type\" : \"long\"}\n" +
            "            }\n" +
            "        }\n" +
            "    }";

    protected static final String MAPPING_SEND_STRING = "{\n" +
            "    \t\"send\" : {\n" +
            "            \"properties\" : {\n" +
            "                \"created\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"start\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"nodeId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeGroup\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeHost\" : {\"type\" : \"ip\", \"index\" : \"not_analyzed\"},\n" +
            "                \"sendHost\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"exception\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventName\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"mdcValue\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"success\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"took\" : {\"type\" : \"long\"}\n" +
            "            }\n" +
            "        }\n" +
            "    }";

    protected static final String MAPPING_RECEIVED_STRING = "{\n" +
            "    \t\"received\" : {\n" +
            "            \"properties\" : {\n" +
            "                \"created\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"start\" :{\"type\" : \"date\", \"format\":\"date_time\"},\n" +
            "                \"nodeId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"fromNodeId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeGroup\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"nodeHost\" : {\"type\" : \"ip\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventId\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"eventName\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"mdcValue\" : {\"type\" : \"string\", \"index\" : \"not_analyzed\"},\n" +
            "                \"delay\" : {\"type\" : \"long\"}\n" +
            "            }\n" +
            "        }\n" +
            "    }";

    protected JestClient jestClient;

    /**
     * 连接elastic search的服务地址
     */
    private String elasticHost;

    /**
     * 保存node信息的elastic 的索引
     */
    private String nodeIndex = INDEX_NODE;

    /**
     * 映射node的索引语句
     */
    private String nodeMapping = MAPPING_NODE_STRING;

    /**
     * 保存事件内容的索引
     */
    private String eventIndex = INDEX_EVENT;

    /**
     * 映射event的索引语句
     */
    private String eventMapping = MAPPING_EVENT_STRING;

    /**
     * 发送事件的监控索引
     */
    private String sendIndex = INDEX_SEND;

    /**
     * 发送事件的索引mapping
     */
    private String sendMapping = MAPPING_SEND_STRING;

    /**
     * 接收事件的监控索引
     */
    private String receivedIndex = INDEX_RECEIVED;

    /**
     * 接收事件的索引mapping
     */
    private String receivedMapping = MAPPING_RECEIVED_STRING;

    /**
     * 连接超时时间
     */
    private Integer connTimeout;

    /**
     * 读取超时时间
     */
    private Integer readTimeout;

    private volatile boolean open;

    /**
     * 推送nodeInfo信息的间隔，这里他会做缓冲，缓冲30秒，然后批量提交到ElasticSearch，最小间隔为30秒
     */
    private Integer nodeInfoPushInterval = MINMUM_PUSH_NODE_INFO_INTERVAL;

    private Date lastPushNodeInfoTime = null;

    private List<NodeInfo> nodeInfoCache = new ArrayList<NodeInfo>();

    private final Logger logger = Logger.getLogger(this.getClass());

    public ElasticSearchInfoForward(){

    }

    @PostConstruct
    public void startup() throws IOException {
        if(open)
            return ;

        if(StringHelper.isEmpty(elasticHost))
            throw new IllegalArgumentException("please set parameter of elasticHost");
        ElasticSearchClientFactory factory = new ElasticSearchClientFactory();
        factory.setElasticHost(elasticHost);
        factory.setConnTimeout(connTimeout);
        factory.setReadTimeout(readTimeout);
        this.jestClient = factory.createClient();
        // init index
        handleIndex(nodeIndex, "node", nodeMapping);
        handleIndex(eventIndex, "event", eventMapping);
        handleIndex(sendIndex, "send", sendMapping);
        handleIndex(receivedIndex, "received", receivedMapping);
        logger.info(String.format("startup jest client[%s]success.", this.elasticHost));
    }

    @PreDestroy
    public void shutdown(){
        if(!open)
            return ;

        this.jestClient.shutdownClient();
        logger.info(String.format("shutdown jest client[%s]success.", this.elasticHost));
    }

    protected void handleIndex(String index, String type, String mapping) throws IOException {
        if(!existsIndex(index)){
            JestResult result = createIndex(index);
            if(!result.isSucceeded()){
                throw new IllegalStateException(String.format("create %s index error:%s", index, result.getErrorMessage()));
            }
            logger.info(String.format("create elastic index[%s]success", index));
            result = createMapping(index, type, mapping);
            if(!result.isSucceeded()){
                throw new IllegalStateException(String.format("mapping %s type error:%s", index, result.getErrorMessage()));
            }
            logger.info(String.format("mapping elastic type[%s]success", type));
        }
    }

    protected boolean existsIndex(String index) throws IOException {
        return jestClient.execute(new IndicesExists.Builder(index).build()).isSucceeded();
    }

    protected JestResult createIndex(String index) throws IOException {
        return jestClient.execute(new CreateIndex.Builder(index).build());
    }

    protected JestResult createMapping(String index, String type, String value) throws IOException {
        PutMapping putMapping = new PutMapping.Builder(
                index,
                type,
                value
        ).build();
        return jestClient.execute(putMapping);
    }

    @Override
    public void forwardNodeInfo(NodeInfo info) {
        info.setTimestamp(new Date());
        if(lastPushNodeInfoTime == null){
            lastPushNodeInfoTime = new Date();
        }
        if(new Date().before(DateUtils.addSeconds(lastPushNodeInfoTime, nodeInfoPushInterval))){
            try {
                nodeInfoCache.add(info.clone());
            } catch (CloneNotSupportedException e) {
                logger.error(e.getMessage(), e);
            }
            return ;
        }
        lastPushNodeInfoTime = new Date();
        if(nodeInfoCache.size() == 0)
            return ;

        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for(NodeInfo nodeInfo : nodeInfoCache){
            Index index = new Index.Builder(nodeInfo).index(nodeIndex).type("node").build();
            bulkBuilder.addAction(index);
        }

        try {
            BulkResult result = jestClient.execute(bulkBuilder.build());
            if(!result.isSucceeded())
                logger.error("put node index data error:" + result.getJsonString());
        } catch (Throwable e) {
            logger.error("put node index data error:" + e.getMessage());
        }finally {
            nodeInfoCache.clear();
        }
    }

    @Override
    public void forwardEventInfo(List<MonitorEventInfo> infos) {
        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for(MonitorEventInfo info : infos){
            Index index = null;
            if(info.getType() == null || info.getType().intValue() == MonitorEventInfo.TYPE_CONSUMED)
                index = new Index.Builder(info).index(eventIndex).type("event").build();
            else if(info.getType().intValue() == MonitorEventInfo.TYPE_SEND){
                index = new Index.Builder(info).index(sendIndex).type("send").build();
            }
            else if(info.getType().intValue() == MonitorEventInfo.TYPE_RECEIVED){
                index = new Index.Builder(info).index(receivedIndex).type("received").build();
            }
            if(null != index)
                bulkBuilder.addAction(index);
        }
        try {
            BulkResult result = jestClient.execute(bulkBuilder.build());
            if(!result.isSucceeded()) {
                String errorInfo = result.getJsonString();
                if(errorInfo.contains("\"status\":429")){
                    throw new RuntimeException("access elastic search error:status:429, it would retry");
                }
                logger.error("put event index data error:" + errorInfo);
            }
        } catch (Throwable e) {
            logger.error("put event index data error:" + e.getMessage());
        }
    }

    public String getElasticHost() {
        return elasticHost;
    }

    public void setElasticHost(String elasticHost) {
        this.elasticHost = elasticHost;
    }

    public Integer getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(Integer connTimeout) {
        this.connTimeout = connTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(String nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public String getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(String eventIndex) {
        this.eventIndex = eventIndex;
    }

    public String getNodeMapping() {
        return nodeMapping;
    }

    public void setNodeMapping(String nodeMapping) {
        this.nodeMapping = nodeMapping;
    }

    public String getEventMapping() {
        return eventMapping;
    }

    public void setEventMapping(String eventMapping) {
        this.eventMapping = eventMapping;
    }

    public Integer getNodeInfoPushInterval() {
        return nodeInfoPushInterval;
    }

    public String getSendIndex() {
        return sendIndex;
    }

    public void setSendIndex(String sendIndex) {
        this.sendIndex = sendIndex;
    }

    public String getSendMapping() {
        return sendMapping;
    }

    public void setSendMapping(String sendMapping) {
        this.sendMapping = sendMapping;
    }

    public String getReceivedIndex() {
        return receivedIndex;
    }

    public void setReceivedIndex(String receivedIndex) {
        this.receivedIndex = receivedIndex;
    }

    public String getReceivedMapping() {
        return receivedMapping;
    }

    public void setReceivedMapping(String receivedMapping) {
        this.receivedMapping = receivedMapping;
    }

    public void setNodeInfoPushInterval(Integer nodeInfoPushInterval) {
        if(null == nodeInfoPushInterval)
            throw new IllegalArgumentException("parameter of nodeInfoPushInterval has to be set");
        if(nodeInfoPushInterval < MINMUM_PUSH_NODE_INFO_INTERVAL){
            throw new IllegalArgumentException("parameter of nodeInfoPushInterval has to be more or equal than 30 sec");
        }
        this.nodeInfoPushInterval = nodeInfoPushInterval;
    }
}
