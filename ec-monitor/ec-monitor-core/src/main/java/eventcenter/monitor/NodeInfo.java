package eventcenter.monitor;

import java.io.Serializable;
import java.util.Date;

/**
 * 加入监控后，每个事件中心就是一个事件节点，那么节点就需要包含一些属性，例如唯一的ID，IP地址、hostName、启动时间、健康状况、空闲状态等等基本信息
 * Created by liumingjian on 16/2/15.
 */
public class NodeInfo implements Serializable,Cloneable{

    private static final long serialVersionUID = -8511715493140393011L;

    /**
     * 节点唯一编号
     */
    private String id;

    /**
     * 节点分组
     */
    private String group;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 启动时间
     */
    private Date start;

    /**
     * 健康状态，0表示掉线、1表示空闲、2表示较忙，3表示非常繁忙
     */
    private Integer stat;

    /**
     * 查看缓存队列所缓存的事件数量
     */
    private Integer queueSize;

    /**
     * 容器中正在运行的线程并发数
     */
    private Integer countOfLiveThread;

    /**
     * 获取容器中缓冲的队列容量
     */
    private Integer countOfQueueBuffer;

    /**
     * 创建的事件戳
     */
    private Date timestamp;

    /**
     * 节点唯一编号
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 节点唯一编号
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 节点名称
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 节点名称
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 主机地址
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * 主机地址
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 启动时间
     * @return
     */
    public Date getStart() {
        return start;
    }

    /**
     * 启动时间
     * @param start
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * 健康状态，0表示掉线、1表示空闲、2表示较忙，3表示非常繁忙
     * @return
     */
    public Integer getStat() {
        return stat;
    }

    /**
     * 健康状态，0表示掉线、1表示空闲、2表示较忙，3表示非常繁忙
     * @param stat
     */
    public void setStat(Integer stat) {
        this.stat = stat;
    }

    /**
     * 节点分组
     * @return
     */
    public String getGroup() {
        return group;
    }

    /**
     * 节点分组
     * @param group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 查看缓存队列所缓存的事件数量
     * @return
     */
    public Integer getQueueSize() {
        return queueSize;
    }

    /**
     * 查看缓存队列所缓存的事件数量
     * @param queueSize
     */
    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * 容器中正在运行的线程并发数
     * @return
     */
    public Integer getCountOfLiveThread() {
        return countOfLiveThread;
    }

    /**
     * 容器中正在运行的线程并发数
     * @param countOfLiveThread
     */
    public void setCountOfLiveThread(Integer countOfLiveThread) {
        this.countOfLiveThread = countOfLiveThread;
    }

    /**
     * 获取容器中缓冲的队列容量
     * @return
     */
    public Integer getCountOfQueueBuffer() {
        return countOfQueueBuffer;
    }

    /**
     * 获取容器中缓冲的队列容量
     * @param countOfQueueBuffer
     */
    public void setCountOfQueueBuffer(Integer countOfQueueBuffer) {
        this.countOfQueueBuffer = countOfQueueBuffer;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public NodeInfo clone() throws CloneNotSupportedException {
        return (NodeInfo)super.clone();
    }
}
