package eventcenter.api;

/**
 * 当事件触发filter时，如果有监听器准备执行，那么会执行这个IEVentFireFilter，这个filter和{@link ListenerFilter}不同的是，
 * 前者是在fireEvent方法中执行调用，优先{@link ListenerFilter}，并且前者条件判断，后者是可以通过执行filter的条件判断是否需要继续执行这个监听器。
 * 后者由于经过了事件执行容器，所以前者更加优先，所以实现这个filter要求要快速执行，尽可能短时间内执行完成，因为他会对调用fireEvent的地方产生阻塞
 *
 * @author liumingjian
 * @date 2017/4/5
 */
public interface EventFireFilter extends EventFilter {

    /**
     * 当调用{@link EventCenter#fireEvent(Object, EventInfo, Object)}时，将会触发这个方法
     * @param target
     * @param eventInfo
     * @param result
     */
    void onFired(Object target, EventInfo eventInfo, Object result);
}
