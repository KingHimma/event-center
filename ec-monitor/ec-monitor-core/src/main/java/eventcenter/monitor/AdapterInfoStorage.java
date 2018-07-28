package eventcenter.monitor;

import java.util.List;

/**
 * 这个是默认的实现，方法中不做任何实现
 * Created by liumingjian on 2017/4/6.
 */
public class AdapterInfoStorage implements InfoStorage {
    @Override
    public void open() throws Exception {
        // Nothing to implement
    }

    @Override
    public void close() throws Exception {
        // Nothing to implement
    }

    @Override
    public void pushEventInfos(List<MonitorEventInfo> infos) {
        // Nothing to implement
    }

    @Override
    public void pushEventInfo(MonitorEventInfo info) {
        // Nothing to implement
    }

    @Override
    public List<MonitorEventInfo> popEventInfos(int maxSize) {
        // Nothing to implement
        return null;
    }

    @Override
    public MonitorEventInfo popEventInfo() {
        // Nothing to implement
        return null;
    }

    @Override
    public void saveNodeInfo(NodeInfo nodeInfo) {
        // Nothing to implement
    }

    @Override
    public NodeInfo queryNodeInfo() {
        // Nothing to implement
        return null;
    }
}
