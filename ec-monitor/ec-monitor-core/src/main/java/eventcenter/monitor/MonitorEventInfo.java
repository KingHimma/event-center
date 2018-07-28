package eventcenter.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import eventcenter.api.CommonEventSource;
import eventcenter.api.EventInfo;
import eventcenter.api.ListenerReceipt;
import eventcenter.remote.Target;
import eventcenter.remote.publisher.PublisherGroup;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;

/**
 * 通过拦截事件执行之后的回执信息，封装为监控的事件信息
 * Created by liumingjian on 16/2/15.
 */
public class MonitorEventInfo implements Serializable {

    private static final long serialVersionUID = 170864056603883355L;

    public static final Integer TYPE_CONSUMED = 0;

    public static final Integer TYPE_SEND = 1;

    public static final Integer TYPE_RECEIVED = 2;

    /**
     * 当前主机地址
     */
    private static InetAddress clientIp;

    /**
     * 当前事件中心消费的节点编号
     */
    private String nodeId;

    /**
     * 生产事件节点的分组名称
     */
    private String nodeGroup;

    /**
     * 事件节点的主机地址
     */
    private String nodeHost;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 来自生产事件的事件节点，来自远程的，如果是自生产自消费的，那么这个为空
     */
    private String fromNodeId;

    /**
     * if listener executed success, it would calculate which listener consumed event time.
     */
    private Long took;

    /**
     * 事件从创建到开始消费的延迟时间
     */
    private Long delay;

    /**
     * if listener executed failure, exception would be set.
     */
    private Throwable exception;

    /**
     * if listener executed success, it would be true.
     */
    private boolean success;

    /**
     * executed event listener
     */
    private String listenerClazz;

    /**
     * 事件生产的时间
     */
    private Date created;

    /**
     * 事件开始消费的时间
     */
    private Date start;

    /**
     * time of consumed
     */
    private Date consumed;

    /**
     * json type
     */
    private String eventArgs;

    /**
     * json type
     */
    private String eventResult;

    private String eventId;

    private String eventName;

    private String mdcValue;

    /**
     * 监控事件信息类型，0表示消费的事件，1表示发送的事件，2表示接收的事件
     */
    private Integer type = 0;

    /**
     * 如果type = 1，那么这里需要设置发送到远程端的host名称
     */
    private String sendHost;

    /**
     * 异常消息
     */
    private String exceptionMessage;

    /**
     * 异常堆栈
     */
    private String exceptionStack;

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getConsumed() {
        return consumed;
    }

    public void setConsumed(Date consumed) {
        this.consumed = consumed;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSendHost() {
        return sendHost;
    }

    public void setSendHost(String sendHost) {
        this.sendHost = sendHost;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionStack() {
        return exceptionStack;
    }

    public void setExceptionStack(String exceptionStack) {
        this.exceptionStack = exceptionStack;
    }

    /**
     * 获取当前主机IP地址
     * @return
     * @throws UnknownHostException
     */
    private static InetAddress getSerIp() throws UnknownHostException {
        if(null != clientIp)
            return clientIp;
        // 根据网卡取本机配置的IP
        Enumeration<NetworkInterface> allNetInterfaces;  //定义网络接口枚举类
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();  //获得网络接口
            InetAddress ip = null; //声明一个InetAddress类型ip地址
            while (allNetInterfaces.hasMoreElements()) //遍历所有的网络接口
            {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses(); //同样再定义网络地址枚举类
                while (addresses.hasMoreElements())
                {
                    ip = addresses.nextElement();
                    if (ip != null && (ip instanceof Inet4Address)) //InetAddress类包括Inet4Address和Inet6Address
                    {
                        if(!ip.getHostAddress().equals("127.0.0.1")){
                            clientIp = ip;
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Logger.getLogger(MonitorEventInfo.class).error(e.getMessage(), e);
        }
        clientIp = Inet4Address.getLocalHost();
        return clientIp;
    }

    public static MonitorEventInfo buildSend(NodeInfo nodeInfo, PublisherGroup group, Target target, EventInfo eventInfo, Object eventResult, Exception e){
        MonitorEventInfo info = new MonitorEventInfo();
        info.setType(TYPE_SEND);
        info.setConsumed(null);
        info.setStart(new Date());
        info.setDelay(null);
        info.setEventId(eventInfo.getId());
        info.setEventName(eventInfo.getName());
        info.setException(e);
        info.setMdcValue(eventInfo.getMdcValue());
        info.setNodeGroup(nodeInfo.getGroup());
        info.setNodeHost(nodeInfo.getHost());
        info.setNodeId(nodeInfo.getId());
        info.setNodeName(nodeInfo.getName());
        info.setSuccess(e == null);
        info.setSendHost(group.getRemoteUrl());
        try {
            info.setCreated(target.getCreated());
            if (null != target.getCreated())
                info.setTook(System.currentTimeMillis() - target.getCreated().getTime());
        }catch(Throwable ex){

        }
        return info;
    }

    public static MonitorEventInfo buildReceived(NodeInfo nodeInfo, Target target, EventInfo eventInfo, Object eventResult){
        MonitorEventInfo info = new MonitorEventInfo();
        info.setType(TYPE_RECEIVED);
        info.setConsumed(null);
        info.setStart(new Date());
        info.setEventId(eventInfo.getId());
        info.setEventName(eventInfo.getName());
        info.setMdcValue(eventInfo.getMdcValue());
        info.setNodeGroup(nodeInfo.getGroup());
        info.setNodeHost(nodeInfo.getHost());
        info.setNodeId(nodeInfo.getId());
        info.setNodeName(nodeInfo.getName());
        info.setFromNodeId(target.getNodeId());
        info.setSuccess(true);
        try {
            info.setCreated(target.getCreated());
            if (null != target.getCreated()) {
                info.setDelay(System.currentTimeMillis() - target.getCreated().getTime());
            }
        }catch(Throwable ex){

        }
        return info;
    }

    public static MonitorEventInfo buildWith(ListenerReceipt receipt, NodeInfo nodeInfo, Object eventArgs, Object eventResult){
        MonitorEventInfo info = new MonitorEventInfo();
        info.setSuccess(receipt.isSuccess());
        info.setTook(receipt.getTook());
        info.setException(receipt.getException());
        info.setNodeId(nodeInfo.getId());
        info.setCreated(new Date(receipt.getEvt().getTimestamp()));
        info.setStart(receipt.getStart());
        if(null != info.getStart() && null != info.getCreated()){
            info.setDelay(info.getStart().getTime() - info.getCreated().getTime());
        }
        if(null != receipt.getEventListener())
            info.setListenerClazz(receipt.getEventListener().getClass().getName());
        info.setEventId(receipt.getEvt().getEventId());
        info.setEventName(receipt.getEvt().getEventName());
        info.setMdcValue(receipt.getEvt().getMdcValue());
        info.setNodeGroup(nodeInfo.getGroup());
        try {
            info.setNodeHost(getSerIp().getHostAddress());
        } catch (UnknownHostException e) {
            Logger.getLogger(MonitorEventInfo.class).error(e.getMessage(), e);
        }
        if(receipt.getEvt() instanceof CommonEventSource){
            CommonEventSource cevt = (CommonEventSource)receipt.getEvt();
            info.setEventArgs((eventArgs == null)?null:toJSONString(eventArgs));
            info.setEventResult(eventResult==null?null:toJSONString(eventResult));
        }
        if(receipt.getEvt().getSourceInfo() instanceof Target){
            Target target = (Target)receipt.getEvt().getSourceInfo();
            info.setFromNodeId(target.getNodeId());
        }
        return info;
    }

    private static String toJSONString(Object arg){
        return JSON.toJSONString(arg, SerializerFeature.UseISO8601DateFormat);
    }

    public static MonitorEventInfo buildWith(ListenerReceipt receipt, NodeInfo nodeInfo, Object eventArgs, Object eventResult, Date consumed){
        MonitorEventInfo info = buildWith(receipt, nodeInfo, eventArgs, eventResult);
        info.setConsumed(consumed);
        return info;
    }
}
