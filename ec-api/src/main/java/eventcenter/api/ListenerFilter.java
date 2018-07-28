package eventcenter.api;

/**
 * Filter for {@link EventListener}, it can execute before that listener invoked, or executed after listener invoked
 *
 * @author liumingjian
 * @date 16/1/26
 */
public interface ListenerFilter {

    /**
     * execute filter before listener invoked
     * @param listener
     * @param evt
     * @return 如果拦截器执行处理正常，应该返回true，如果返回false，那么事件和后置拦截器都不会执行
     */
    boolean before(EventListener listener, EventSourceBase evt);

    /**
     * execute filter after listener invoked
     */
    void after(ListenerReceipt receipt);
}
