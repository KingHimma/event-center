package eventcenter.leveldb.tx;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liumingjian on 2017/3/1.
 */
public class TxnRef implements Serializable{
    private static final long serialVersionUID = 7031493186689586109L;

    /**
     * 这个txnId关联的是存储在event的key，也就是放置在leveldb中的key
     */
    private String txnId;

    private List<String> bucketTxnIds;

    /**
     * 事务数量
     */
    private int txnCount = 0;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public List<String> getBucketTxnIds() {
        if(null == bucketTxnIds){
            bucketTxnIds = new LinkedList<String>();
        }
        return bucketTxnIds;
    }

    public void setBucketTxnIds(List<String> bucketTxnIds) {
        this.bucketTxnIds = bucketTxnIds;
    }

    public int getTxnCount() {
        return txnCount;
    }

    public void increaseTxnCount(){
        ++txnCount;
    }

    public void decreaseTxnCount(){
        if(txnCount <= 0)
            return ;
        --txnCount;
    }
}
