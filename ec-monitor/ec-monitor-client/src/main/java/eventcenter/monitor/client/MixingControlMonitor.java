package eventcenter.monitor.client;

import eventcenter.api.ListenerReceipt;
import eventcenter.monitor.*;
import eventcenter.remote.utils.StringHelper;
import eventcenter.monitor.AbstractControlMonitor;

import java.util.HashSet;
import java.util.Set;

/**
 * 混合多个不同的IControlMonitor实例
 *
 * @author liumingjian
 * @date 2017/4/10
 */
public class MixingControlMonitor extends AbstractControlMonitor {

    Set<AbstractControlMonitor> monitors;

    public MixingControlMonitor(){
        delayInitLock.lock();
    }

    @Override
    protected InfoStorage loadInfoStorage() {
        return new AdapterInfoStorage();
    }

    @Override
    public void startup() {
        if(open) {
            return ;
        }
        delayInitLock.unlock();
        if(getMonitors().size() == 0) {
            throw new IllegalArgumentException("please set parameter of monitors");
        }
        nodeInfo = loadNodeInfo();
        for(AbstractControlMonitor monitor : getMonitors()){
            if(StringHelper.isEmpty(monitor.getGroup())){
                monitor.setGroup(this.getGroup());
            }
            if(StringHelper.isEmpty(monitor.getNodeName())){
                monitor.setNodeName(this.getNodeName());
            }
            monitor.startup();
        }
        open = true;
    }

    @Override
    public void shutdown() {
        if(!open) {
            return;
        }
        for(ControlMonitor monitor : getMonitors()){
            monitor.shutdown();
        }
        open = false;
    }

    @Override
    public void saveEventInfo(MonitorEventInfo mei) {
        for(ControlMonitor monitor : getMonitors()){
            try{
                monitor.saveEventInfo(mei);
            }catch(Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void saveListenerReceipt(ListenerReceipt receipt) {
        for(ControlMonitor monitor : getMonitors()){
            try{
                monitor.saveListenerReceipt(receipt);
            }catch(Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Set<AbstractControlMonitor> getMonitors() {
        if(null == monitors)
            monitors = new HashSet<AbstractControlMonitor>();
        return monitors;
    }

    public void setMonitors(Set<AbstractControlMonitor> monitors) {
        this.monitors = monitors;
    }
}
