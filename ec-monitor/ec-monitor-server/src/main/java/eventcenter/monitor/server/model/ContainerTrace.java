package eventcenter.monitor.server.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 容器运行的数据
 * Created by liumingjian on 16/5/31.
 */
public class ContainerTrace implements Serializable {
    private static final long serialVersionUID = 400393261118147636L;

    private String nodeId;

    private Integer queueSize;

    private Integer countOfLiveThread;

    private Integer countOfQueueBuffer;

    private Date created;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getCountOfLiveThread() {
        return countOfLiveThread;
    }

    public void setCountOfLiveThread(Integer countOfLiveThread) {
        this.countOfLiveThread = countOfLiveThread;
    }

    public Integer getCountOfQueueBuffer() {
        return countOfQueueBuffer;
    }

    public void setCountOfQueueBuffer(Integer countOfQueueBuffer) {
        this.countOfQueueBuffer = countOfQueueBuffer;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
