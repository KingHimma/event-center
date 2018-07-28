package eventcenter.leveldb.tx;

import eventcenter.remote.utils.StringHelper;
import org.iq80.leveldb.DB;

/**
 * Created by liumingjian on 2016/12/29.
 */
public class TxnQueueComponentFactory {

    public static TxnQueueComponent create(String queueName, DB db, TransactionConfig config, Integer txnCapacity){
        if(StringHelper.isEmpty(queueName))
            throw new IllegalArgumentException("required queueName parameter");
        if(db == null)
            throw new IllegalArgumentException("required db parameter");
        if(config == null)
            throw new IllegalArgumentException("required config parameter");
        if(txnCapacity == null)
            throw new IllegalArgumentException("required txnCapacity parameter");
        TxnQueueComponent component = new TxnQueueComponent(queueName, db);
        component.setTxnCapacity(txnCapacity);
        if(config.getDiscardRate() != null){
            component.setDiscardCapacity(txnCapacity * config.getDiscardRate());
        }
        if(config.getFailureRate() != null){
            component.setFailureCapacity(txnCapacity * config.getFailureRate());
        }
        component.setOpenRetry(config.isOpenRetry());
        component.setRetryCount(config.getRetryCount());
        return component;
    }
}
