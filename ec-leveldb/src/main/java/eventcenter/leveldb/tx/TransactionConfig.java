package eventcenter.leveldb.tx;

import java.io.Serializable;

/**
 * 事务配置
 * Created by liumingjian on 2016/12/29.
 */
public class TransactionConfig implements Serializable {

    public static final Integer MINIMUM_FAILURE_RATE = 5;

    public static final Integer MINIMUM_DISCARD_RATE = 5;

    private static final long serialVersionUID = -5678926672668704459L;

    /**
     * 最多可存储失败事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     */
    private Integer failureRate = MINIMUM_FAILURE_RATE;

    /**
     * 最多可存储丢弃事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     */
    private Integer discardRate = MINIMUM_DISCARD_RATE;

    /**
     * 重试事务的次数
     */
    private Integer retryCount = 3;

    /**
     * 事务超时时间
     */
    private Integer txnTimeout = 60;

    /**
     * 是否开启，超时或者事件消费异常的重试机制
     */
    private boolean openRetry = false;

    /**
     * 最多可存储失败事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     * @return
     */
    public Integer getFailureRate() {
        return failureRate;
    }

    /**
     * 最多可存储失败事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     * @param failureRate
     */
    public void setFailureRate(Integer failureRate) {
        if(null == failureRate)
            throw new IllegalArgumentException("failureRate is null");
        if(failureRate < MINIMUM_FAILURE_RATE){
            throw new IllegalArgumentException("failureRate value can't be lower than " + MINIMUM_FAILURE_RATE);
        }
        this.failureRate = failureRate;
    }

    /**
     * 最多可存储丢弃事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     * @return
     */
    public Integer getDiscardRate() {
        return discardRate;
    }

    /**
     * 最多可存储丢弃事务的容量倍率，相比txnCapacity的倍率，默认为5，不可低于5
     * @param discardRate
     */
    public void setDiscardRate(Integer discardRate) {
        if(null == discardRate)
            throw new IllegalArgumentException("discardRate is null");
        if(discardRate < MINIMUM_DISCARD_RATE){
            throw new IllegalArgumentException("discardRate value can't be lower than " + MINIMUM_DISCARD_RATE);
        }
        this.discardRate = discardRate;
    }

    /**
     * 事务失败的重试次数
     * @return
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 事务失败的重试次数
     * @param retryCount
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 事务超时时间
     * @return
     */
    public Integer getTxnTimeout() {
        return txnTimeout;
    }

    /**
     * 事务超时时间
     * @param txnTimeout
     */
    public void setTxnTimeout(Integer txnTimeout) {
        this.txnTimeout = txnTimeout;
    }

    /**
     * 是否开启，超时或者事件消费异常的重试机制，默认不开启
     * @return
     */
    public boolean isOpenRetry() {
        return openRetry;
    }

    /**
     * 是否开启，超时或者事件消费异常的重试机制
     * @param openRetry
     */
    public void setOpenRetry(boolean openRetry) {
        this.openRetry = openRetry;
    }
}
