package eventcenter.api.aggregator;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;

/**
 * 这个是为了支持IEventListener接口的调用
 *
 * @author liumingjian
 * @date 16/8/1
 */
public class AggregatorListenerWrapper implements AggregatorEventListener {

    protected final EventListener eventListener;

    public AggregatorListenerWrapper(EventListener eventListener){
        this.eventListener = eventListener;
    }

    @Override
    public void onObserved(CommonEventSource source) {
        this.eventListener.onObserved(source);
    }
}
