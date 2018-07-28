package eventcenter.leveldb.tx;

import java.io.Serializable;

/**
 * 有关 {@link TxnQueueComponent}包含的相关配置和数据信息
 * Created by liumingjian on 2016/12/28.
 */
public class TxnQueueInfo implements Serializable {
    private static final long serialVersionUID = -6479375917557005302L;

    private String txnBucketId;

    private String failureBucketId;

    private String discardBucketId;

    public String getTxnBucketId() {
        return txnBucketId;
    }

    public void setTxnBucketId(String txnBucketId) {
        this.txnBucketId = txnBucketId;
    }

    public String getFailureBucketId() {
        return failureBucketId;
    }

    public void setFailureBucketId(String failureBucketId) {
        this.failureBucketId = failureBucketId;
    }

    public String getDiscardBucketId() {
        return discardBucketId;
    }

    public void setDiscardBucketId(String discardBucketId) {
        this.discardBucketId = discardBucketId;
    }
}
