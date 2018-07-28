package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventListenerTask;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.tx.EventTxnStatus;

/**
 *
 * @author liumingjian
 * @date 2017/1/3
 */
public class TxEventListenerTask extends EventListenerTask {

    protected final EventTxnStatus txn;

    protected final LevelDBContainer container;

    public TxEventListenerTask(EventListener listener, CommonEventSource evt, LevelDBContainer container, EventTxnStatus txn) {
        super(listener, evt);
        this.txn = txn;
        this.container = container;
    }

    public TxEventListenerTask(EventListener listener, CommonEventSource evt, Boolean allowThrowException, LevelDBContainer container, EventTxnStatus txn) {
        super(listener, evt, allowThrowException);
        this.txn = txn;
        this.container = container;
    }

    @Override
    protected void afterExecute(ListenerReceipt receipt) {
        try {
            this.container.commitTransaction(txn);
        }catch(Throwable e){
            logger.error("commit transaction error:" + e.getMessage(), e);
        }
        super.afterExecute(receipt);
    }
}
