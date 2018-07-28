package eventcenter.builder.monitor.log;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.MonitorConfig;
import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.client.LogControlMonitor;

/**
 *
 * @author liumingjian
 * @date 2017/9/15
 */
public class LogMonitorConfig extends MonitorConfig {
    private static final long serialVersionUID = 3924019641093658379L;

    @Override
    protected AbstractControlMonitor createControlMonitor(DefaultEventCenter eventCenter, boolean subscriber) {
        return new LogControlMonitor();
    }
}
