package eventcenter.api;

/**
 * 当事件执行完成之后，如果 {@link EventListenerTask}执行完事件中的onObserved方法之后，无论执行成功还是失败，都会立即调用这个回调接口
 *
 * @author liumingjian
 * @date 2016/12/23
 */
public interface ListenerExecuted {

    void afterExecuted(EventSourceBase event, EventListener eventListener, Throwable e);
}
