package eventcenter.builder.monitor.mixing;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.MonitorConfig;
import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.client.MixingControlMonitor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liumingjian
 * @date 2018/5/3
 **/
public class MixingMonitorConfig extends MonitorConfig {
    private static final long serialVersionUID = -113106029143855114L;

    private List<MonitorConfig> monitorConfigs = new ArrayList<MonitorConfig>();

    @Override
    protected AbstractControlMonitor createControlMonitor(DefaultEventCenter eventCenter, boolean subscriber) {
        MixingControlMonitor monitor = new MixingControlMonitor();
        monitor.setNodeName(this.getNodeName());
        if(null != this.getHeartbeatInterval()) {
            monitor.setHeartbeatInterval(this.getHeartbeatInterval());
        }
        if(null != this.getSaveEventData()) {
            monitor.setSaveEventData(this.getSaveEventData());
        }
        for(MonitorConfig innerConfig : monitorConfigs){
            if(!StringUtils.hasText(innerConfig.getNodeName())){
                innerConfig.setNodeName(monitor.getNodeName());
            }
            if(innerConfig.getHeartbeatInterval() == null){
                innerConfig.setHeartbeatInterval(monitor.getHeartbeatInterval());
            }
            if(innerConfig.getSaveEventData() == null){
                innerConfig.setSaveEventData(this.getSaveEventData());
            }
            innerConfig.setLoadFilters(false);
            monitor.getMonitors().add(innerConfig.load(eventCenter, subscriber));
        }
        return monitor;
    }

    public List<MonitorConfig> getMonitorConfigs() {
        return monitorConfigs;
    }

    public void setMonitorConfigs(List<MonitorConfig> monitorConfigs) {
        this.monitorConfigs = monitorConfigs;
    }
}
