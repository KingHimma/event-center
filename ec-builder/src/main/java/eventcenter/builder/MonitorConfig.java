package eventcenter.builder;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.dubbo.DubboConfigContext;
import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.client.filter.ListenerExecutedFilter;
import eventcenter.monitor.client.filter.RemoteFilter;
import eventcenter.remote.publisher.PublishEventCenter;

import java.io.Serializable;

/**
 * 监控配置
 *
 * @author liumingjian
 * @date 2017/9/14
 */
public abstract class MonitorConfig implements Serializable {
    private static final long serialVersionUID = 794004761466469150L;

    protected AbstractControlMonitor monitor;

    protected Boolean saveEventData;

    protected Long heartbeatInterval;

    protected String nodeName;

    protected boolean loadFilters = true;

    protected volatile boolean loaded = false;

    public Boolean getSaveEventData() {
        return saveEventData;
    }

    public void setSaveEventData(Boolean saveEventData) {
        this.saveEventData = saveEventData;
    }

    public Long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    protected abstract AbstractControlMonitor createControlMonitor(DefaultEventCenter eventCenter, boolean subscriber);

    public boolean isLoadFilters() {
        return loadFilters;
    }

    public void setLoadFilters(boolean loadFilters) {
        this.loadFilters = loadFilters;
    }

    /**
     * 加载monitor，如果loadFilters为true，则会加载filters，并且启动这个monitor，如果为false则不会加载，需要在外部手动加载，并启动它
     * @param eventCenter
     */
    public AbstractControlMonitor load(DefaultEventCenter eventCenter, boolean subscriber){
        if(loaded) {
            return monitor;
        }
        monitor = initControlMonitor(createControlMonitor(eventCenter, subscriber), eventCenter);
        if(loadFilters) {
            loadFilters(monitor, eventCenter, subscriber);
            monitor.startup();
        }
        loaded = true;
        return monitor;
    }

    protected void loadFilters(AbstractControlMonitor controlMonitor, DefaultEventCenter eventCenter, boolean subscriber){
        ListenerExecutedFilter listenerExecutedFilter = new ListenerExecutedFilter();
        listenerExecutedFilter.setControlMonitor(controlMonitor);
        eventCenter.getEcConfig().getGlobalFilters().add(listenerExecutedFilter);
        // 加载FiredEventFilter

        if(eventCenter instanceof PublishEventCenter || subscriber){
            // 创建事件的发送和接收的过滤器
            RemoteFilter remoteFilter = new RemoteFilter();
            remoteFilter.setControlMonitor(controlMonitor);
            eventCenter.getEcConfig().getModuleFilters().add(remoteFilter);
        }
    }

    protected AbstractControlMonitor initControlMonitor(AbstractControlMonitor monitor, DefaultEventCenter eventCenter){
        monitor.setEventCenter(eventCenter);
        monitor.setGroup(DubboConfigContext.getInstance().getDubboConfig().getGroupName());
        if(heartbeatInterval != null) {
            monitor.setHeartbeatInterval(heartbeatInterval);
        }
        if(null != saveEventData) {
            monitor.setSaveEventData(saveEventData);
        }
        monitor.setNodeName(nodeName);
        return monitor;
    }
}
