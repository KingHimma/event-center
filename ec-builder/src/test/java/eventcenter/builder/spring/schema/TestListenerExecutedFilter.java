package eventcenter.builder.spring.schema;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.ListenerFilter;
import eventcenter.api.ListenerReceipt;

/**
 * Created by liumingjian on 2017/9/29.
 */
public class TestListenerExecutedFilter implements ListenerFilter {
    @Override
    public boolean before(EventListener listener, CommonEventSource evt) {
        return false;
    }

    @Override
    public void after(ListenerReceipt receipt) {

    }
}
