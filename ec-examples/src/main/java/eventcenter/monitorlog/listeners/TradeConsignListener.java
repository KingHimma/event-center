package eventcenter.monitorlog.listeners;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.monitor.Trade;
import org.springframework.stereotype.Component;

/**
 * Created by liumingjian on 16/2/28.
 */
@Component
@ListenerBind("trade.consign")
public class TradeConsignListener implements EventListener {

    @Override
    public void onObserved(CommonEventSource source) {
        CommonEventSource evt = source;
        Trade trade = evt.getArg(0, Trade.class);
        //System.out.println("收到事件" + evt.getEventName() + "，tid:" + trade.getTid());
    }
}
