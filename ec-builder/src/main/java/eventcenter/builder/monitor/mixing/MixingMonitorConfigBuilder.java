package eventcenter.builder.monitor.mixing;

import eventcenter.builder.MonitorConfig;

/**
 * @author liumingjian
 * @date 2018/5/3
 **/
public class MixingMonitorConfigBuilder {

    MixingMonitorConfig config;

    protected MixingMonitorConfig getConfig(){
        if(null == config){
            config = new MixingMonitorConfig();
        }
        return config;
    }

    public MixingMonitorConfigBuilder saveEventData(Boolean saveEventData){
        getConfig().setSaveEventData(saveEventData);
        return this;
    }

    public MixingMonitorConfigBuilder heartbeatInterval(Long heartbeatInterval){
        getConfig().setHeartbeatInterval(heartbeatInterval);
        return this;
    }

    public MixingMonitorConfigBuilder nodeName(String nodeName){
        getConfig().setNodeName(nodeName);
        return this;
    }

    public MixingMonitorConfigBuilder addMonitorConfig(MonitorConfig config){
        getConfig().getMonitorConfigs().add(config);
        return this;
    }

    public MixingMonitorConfig build(){
        return getConfig();
    }
}
