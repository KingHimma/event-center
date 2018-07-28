package eventcenter.api.tx;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;

import java.io.Serializable;
import java.util.Date;

/**
 * 事件状态，当通过{@link TransactionalSupport#getTxnStatus(EventSourceBase, EventListener)}方法可以创建多个事件状态
 * Created by liumingjian on 2016/12/23.
 */
public class EventTxnStatus implements Serializable{

    private static final long serialVersionUID = -6791595227414038598L;

    /**
     * 事务编号
     */
    private String txnId;

    /**
     * 事件引用
     */
    private EventSourceBase event;

    /**
     * 事件id
     */
    private String eventId;

    /**
     * 事务开始时间
     */
    private Date start;

    /**
     * 消费事件的监听器类型
     */
    private Class<? extends EventListener> listenerType;

    /**
     * 上一次重试的时间
     */
    private Date lastRetried;

    /**
     * 重试数量
     */
    private int retryCount = 0;

    /**
     * 事务是否结束
     */
    private boolean complete = false;

    /**
     * 事务编号
     * @return
     */
    public String getTxnId() {
        return txnId;
    }

    /**
     * 事务编号
     * @param txnId
     */
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    /**
     * 事件引用
     * @return
     */
    public EventSourceBase getEvent() {
        return event;
    }

    /**
     * 事件引用
     * @param event
     */
    public void setEvent(EventSourceBase event) {
        this.event = event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 事务开始时间
     * @return
     */
    public Date getStart() {
        return start;
    }

    /**
     * 事务开始时间
     * @param start
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * 消费事件的监听器类型
     * @return
     */
    public Class<? extends EventListener> getListenerType() {
        return listenerType;
    }

    /**
     * 消费事件的监听器类型
     * @param listenerType
     */
    public void setListenerType(Class<? extends EventListener> listenerType) {
        this.listenerType = listenerType;
    }

    /**
     * 上一次重试的时间
     * @return
     */
    public Date getLastRetried() {
        return lastRetried;
    }

    /**
     * 上一次重试的时间
     * @param lastRetried
     */
    public void setLastRetried(Date lastRetried) {
        this.lastRetried = lastRetried;
    }

    /**
     * 重试数量
     * @return
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 重试数量
     * @param retryCount
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 事务是否结束
     * @return
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * 事务是否结束
     * @param complete
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
