package eventcenter.api.aggregator;

import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;

/**
 * 这个是为了支持IEventListener接口的调用
 * Created by liumingjian on 16/8/1.
 */
public class AggregatorListenerWrapper implements AggregatorEventListener {

    protected final EventListener eventListener;

    public AggregatorListenerWrapper(EventListener eventListener){
        this.eventListener = eventListener;
    }

    @Override
    public void onObserved(EventSourceBase source) {
        this.eventListener.onObserved(source);
    }
}
