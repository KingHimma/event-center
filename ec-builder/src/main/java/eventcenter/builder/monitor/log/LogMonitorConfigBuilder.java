package eventcenter.builder.monitor.log;

import eventcenter.builder.MonitorConfig;

/**
 * 构建使用日志的监控配置
 * Created by liumingjian on 2017/9/14.
 */
public class LogMonitorConfigBuilder {

    protected LogMonitorConfig monitorConfig;

    public LogMonitorConfig getMonitorConfig(){
        if(null == monitorConfig){
            monitorConfig = new LogMonitorConfig();
        }
        return monitorConfig;
    }

    public LogMonitorConfigBuilder saveEventData(Boolean saveEventData){
        getMonitorConfig().setSaveEventData(saveEventData);
        return this;
    }

    public LogMonitorConfigBuilder heartbeatInterval(Long heartbeatInterval){
        getMonitorConfig().setHeartbeatInterval(heartbeatInterval);
        return this;
    }

    public LogMonitorConfigBuilder nodeName(String nodeName){
        getMonitorConfig().setNodeName(nodeName);
        return this;
    }

    public MonitorConfig build(){
        return getMonitorConfig();
    }
}
