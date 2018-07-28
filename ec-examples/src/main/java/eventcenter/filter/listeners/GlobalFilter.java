package eventcenter.filter.listeners;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.ListenerFilterAdapter;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.annotation.EventFilterable;
import org.springframework.stereotype.Component;

/**
 * Created by liumingjian on 16/1/27.
 */
@EventFilterable(isGlobal = true)
@Component    // 如果使用Spring，需要设置spring的context-scan，并将这个包配置到扫描包中
public class GlobalFilter extends ListenerFilterAdapter {

    @Override
    public boolean before(EventListener listener, EventSourceBase evt) {
        System.out.println("[GlobalFilter] before filter for " + evt.getEventName() + " event, id:" + evt.getEventId());
        return true;
    }

    @Override
    public void after(ListenerReceipt receipt) {
        System.out.println("[GlobalFilter] after filter for " + receipt.getEvt().getEventName() + " event, id:" + receipt.getEvt().getEventId());
    }
}
