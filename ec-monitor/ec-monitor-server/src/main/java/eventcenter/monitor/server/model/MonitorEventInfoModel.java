package eventcenter.monitor.server.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by liumingjian on 16/2/22.
 */
public class MonitorEventInfoModel implements Serializable {

    private static final long serialVersionUID = 405486988049742145L;

    private String id;

    /**
     * 当前事件中心消费的节点编号
     */
    private String nodeId;

    /**
     * 节点主机地址
     */
    private String nodeHost;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点分组名称
     */
    private String nodeGroup;

    /**
     * if listener executed success, it would calculate which listener consumed event time.
     */
    private Long took;

    /**
     * if listener executed failure, exception would be set.
     */
    private String exception;

    /**
     * if listener executed success, it would be true.
     */
    private boolean success;

    /**
     * executed event listener
     */
    private String listenerClazz;

    /**
     * executed event source，JSON格式
     */
    private String eventArgs;

    /**
     * 事件源的结果数据，JSON格式
     */
    private String eventResult;

    /**
     * 来自某个事件中心的节点ID
     */
    private String fromNodeId;

    /**
     * 事件编号
     */
    private String eventId;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件创建的时间
     */
    private Date eventCreate;

    /**
     * 事件消费的时间
     */
    private Date eventConsumed;

    /**
     * 事件日志mdc值
     */
    private String mdcValue;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getListenerClazz() {
        return listenerClazz;
    }

    public void setListenerClazz(String listenerClazz) {
        this.listenerClazz = listenerClazz;
    }

    public String getEventArgs() {
        return eventArgs;
    }

    public void setEventArgs(String eventArgs) {
        this.eventArgs = eventArgs;
    }

    public String getEventResult() {
        return eventResult;
    }

    public void setEventResult(String eventResult) {
        this.eventResult = eventResult;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public Date getEventCreate() {
        return eventCreate;
    }

    public void setEventCreate(Date eventCreate) {
        this.eventCreate = eventCreate;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getMdcValue() {
        return mdcValue;
    }

    public void setMdcValue(String mdcValue) {
        this.mdcValue = mdcValue;
    }

    public Date getEventConsumed() {
        return eventConsumed;
    }

    public void setEventConsumed(Date eventConsumed) {
        this.eventConsumed = eventConsumed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
