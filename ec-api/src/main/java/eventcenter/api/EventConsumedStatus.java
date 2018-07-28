package eventcenter.api;

/**
 * 事件消费的状态
 * Created by liumingjian on 2016/12/23.
 */
public enum EventConsumedStatus {
    /**
     * 消费成功
     */
    SUCCESS(1),

    /**
     * 消费失败
     */
    FAILURE(2),

    /**
     * 消费超时
     */
    TIMEOUT(3);

    private int type;

    EventConsumedStatus(int type){
        this.type = type;
    }
}
