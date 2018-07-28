package eventcenter.monitor.server;

import eventcenter.monitor.InfoForward;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.server.dao.EventContainerTraceCollection;
import eventcenter.monitor.server.dao.EventInfoCollection;
import eventcenter.monitor.server.dao.NodeInfoCollection;
import eventcenter.monitor.server.model.ContainerTrace;
import eventcenter.monitor.server.dao.EventContainerTraceCollection;
import eventcenter.monitor.server.dao.EventInfoCollection;
import eventcenter.monitor.server.dao.NodeInfoCollection;
import eventcenter.monitor.server.model.ContainerTrace;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 实现信息推送的服务
 * Created by liumingjian on 16/2/19.
 */
@Service
public class InfoForwardServer implements InfoForward {

    @Resource
    private EventInfoCollection eventInfoCollection;

    @Resource
    private NodeInfoCollection nodeInfoCollection;

    @Resource
    private EventContainerTraceCollection containerTraceCollection;

    protected final Logger logger = Logger.getLogger(this.getClass());

    /**
     * 用于记录额外的日志，例如往elastic-search中写日志
     */
    protected final Logger traceLogger = Logger.getLogger("node-info-log");

    public EventInfoCollection getEventInfoCollection() {
        return eventInfoCollection;
    }

    public void setEventInfoCollection(EventInfoCollection eventInfoCollection) {
        this.eventInfoCollection = eventInfoCollection;
    }

    public NodeInfoCollection getNodeInfoCollection() {
        return nodeInfoCollection;
    }

    public void setNodeInfoCollection(NodeInfoCollection nodeInfoCollection) {
        this.nodeInfoCollection = nodeInfoCollection;
    }

    @Override
    public void forwardNodeInfo(NodeInfo info) {
        try {
            nodeInfoCollection.save(info);
        }catch(Exception e){
            logger.error(new StringBuilder("save nodeInfo error:").append(e.getMessage()), e);
        }
        try {
            if (info.getQueueSize() != null) {
                containerTraceCollection.insert(toContainerTrace(info));
            }
        }catch(Exception e){
            logger.error(new StringBuilder("save containerTrace error:").append(e.getMessage()), e);
        }
    }

    ContainerTrace toContainerTrace(NodeInfo info){
        ContainerTrace trace = new ContainerTrace();
        trace.setNodeId(info.getId());
        trace.setCountOfQueueBuffer(info.getCountOfQueueBuffer());
        trace.setCountOfLiveThread(info.getCountOfLiveThread());
        trace.setQueueSize(info.getQueueSize());
        trace.setCreated(new Date());
        return trace;
    }

    @Override
    public void forwardEventInfo(List<MonitorEventInfo> infos) {
        try {
            eventInfoCollection.insert(infos);
            if(traceLogger.isTraceEnabled()){
                for(MonitorEventInfo info : infos){
                    traceLogger.trace(info);
                }
            }
        }catch(Exception e){
            logger.error(new StringBuilder("save eventInfos error:").append(e.getMessage()), e);
        }
    }
}
