package eventcenter.monitor.client.filter;

import eventcenter.api.EventInfo;
import eventcenter.monitor.ControlMonitor;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import eventcenter.remote.Target;
import eventcenter.remote.publisher.PublishFilter;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.subscriber.SubscribFilter;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

/**
 * if use ec-remote , it would filter when event send and received , and then record it
 * Created by liumingjian on 2016/11/18.
 */
public class RemoteFilter implements PublishFilter, SubscribFilter {
    @Resource
    private ControlMonitor controlMonitor;

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public boolean afterSend(PublisherGroup group, Target target, EventInfo eventInfo, Object result, Exception e) {
        if(null == controlMonitor){
            logger.error("RemoteFilter didn't set controlMonitor!");
            return true;
        }

        NodeInfo nodeInfo = controlMonitor.queryNodeInfo(false);
        if(null == nodeInfo){
            logger.error("can't query node info from controlMonitor");
            return true;
        }
        MonitorEventInfo monitorEventInfo = MonitorEventInfo.buildSend(nodeInfo, group, target, eventInfo, result, e);
        controlMonitor.saveEventInfo(monitorEventInfo);
        return true;
    }

    @Override
    public boolean afterReceived(Target target, EventInfo eventInfo, Object result) {
        if(null == controlMonitor){
            logger.error("RemoteFilter didn't set controlMonitor!");
            return true;
        }

        NodeInfo nodeInfo = controlMonitor.queryNodeInfo(false);
        if(null == nodeInfo){
            logger.error("can't query node info from controlMonitor");
            return true;
        }
        MonitorEventInfo monitorEventInfo = MonitorEventInfo.buildReceived(nodeInfo, target, eventInfo, result);
        controlMonitor.saveEventInfo(monitorEventInfo);
        return true;
    }

    public ControlMonitor getControlMonitor() {
        return controlMonitor;
    }

    public void setControlMonitor(ControlMonitor controlMonitor) {
        this.controlMonitor = controlMonitor;
    }
}
