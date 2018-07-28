package eventcenter.api.tx;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventContainer;
import eventcenter.api.EventListener;

/**
 * <pre>
 * 事件运行容器的事务支持接口，一般和 {@link EventContainer}配合一起使用，如果事件运行容器支持事务的机制，就需要实现这个接口。
 * 事务主要解决事件从缓存队列中取出之后，如果消费失败或者事件消费运行过程中宕机等等问题，导致数据丢失的问题。当消费的事件未执行commit操作时，
 * 容器需要通过重试机制，保证事件的消费事务一致。
 * 重试机制主要取决于重试次数的最高限制，一般重试3次即可，3次失败之后容器可以丢弃这个事件，并记录相关文档。
 * </pre>
 *
 * @author liumingjian
 * @date 2016/12/23
 */
public interface TransactionalSupport {

    /**
     * 恢复未执行的事务，这个方法需要在第一次调用{@link #getTxnStatus(CommonEventSource, String, EventListener)}方法之前执行，这样就可以恢复执行那些
     * 因为服务故障导致事务未提交的事件
     * @param handler
     */
    void resumeTxn(ResumeTxnHandler handler) throws Exception;

    /**
     * 开始事务，需要将事件和对应的监听器进去注册并开启事务，然后返回事务信息
     * @param event
     * @param txnId 事务编号，这个数据来自缓存队列中的存储的唯一ID
     * @param listener
     */
    EventTxnStatus getTxnStatus(CommonEventSource event, String txnId, EventListener listener)  throws Exception;

    /**
     * 提交事务，如果事务成功执行，将会提交这个事务
     * @param txnStatus
     */
    void commit(EventTxnStatus txnStatus)  throws Exception;

}
