package eventcenter.monitor.client.filter;

import eventcenter.api.ListenerFilterAdapter;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.annotation.EventFilterable;
import eventcenter.monitor.ControlMonitor;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

/**
 * 过滤器，用于拦截所有已经消费的事件
 * Created by liumingjian on 16/2/15.
 */
@EventFilterable(isGlobal = true)
public class ListenerExecutedFilter extends ListenerFilterAdapter {

    @Resource
    private ControlMonitor controlMonitor;

    public ControlMonitor getControlMonitor() {
        return controlMonitor;
    }

    public void setControlMonitor(ControlMonitor controlMonitor) {
        this.controlMonitor = controlMonitor;
    }

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void after(ListenerReceipt receipt) {
        if(null == controlMonitor){
            logger.error("it didn't set controlMonitor!");
            return ;
        }

        controlMonitor.saveListenerReceipt(receipt);
    }
}
