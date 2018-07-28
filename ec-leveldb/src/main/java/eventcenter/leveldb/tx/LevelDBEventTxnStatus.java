package eventcenter.leveldb.tx;

import eventcenter.api.tx.EventTxnStatus;

/**
 * Created by liumingjian on 2016/12/28.
 */
public class LevelDBEventTxnStatus extends EventTxnStatus {
    private static final long serialVersionUID = -3172701079274843135L;

    private Integer pageNo;

    private String bucketId;

    private String bucketTxnId;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketTxnId() {
        return bucketTxnId;
    }

    public void setBucketTxnId(String bucketTxnId) {
        this.bucketTxnId = bucketTxnId;
    }
}
