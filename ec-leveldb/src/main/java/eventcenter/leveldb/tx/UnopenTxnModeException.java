package eventcenter.leveldb.tx;

/**
 * 获取事务时未开启事务模式的异常
 * Created by liumingjian on 2016/12/29.
 */
public class UnopenTxnModeException extends RuntimeException {
    private static final long serialVersionUID = -2496749520142113277L;

    public UnopenTxnModeException(){
        super("LevelDBQueue didn't open txn mode.please set TransactionConfig");
    }
}
