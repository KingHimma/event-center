package eventcenter.monitor.mysql;

import eventcenter.monitor.InfoStorage;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.mysql.dao.ConsumedEventDAO;
import eventcenter.monitor.mysql.dao.ReceivedEventDAO;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author liumingjian
 * @date 2017/4/7
 */
public class MysqlInfoStorage implements InfoStorage {

    @Resource
    ConsumedEventDAO consumedEventDAO;

    @Resource
    ReceivedEventDAO receivedEventDAO;

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void open() throws Exception {
        if(null == consumedEventDAO)
            throw new IllegalArgumentException("please set parameter of consumedEventDAO");
        if(null == receivedEventDAO)
            throw new IllegalArgumentException("please set parameter of receivedEventDAO");
    }

    @Override
    public void close() throws Exception {
        // Nothing to Implement
    }

    @Override
    public void pushEventInfos(List<MonitorEventInfo> infos) {
        // at present, it didn't implement store and forward, so nothing to implement

    }

    @Override
    public void pushEventInfo(MonitorEventInfo info) {
        if(info.getType() == null){
            return ;
        }
        try {
            if (info.getType() == MonitorEventInfo.TYPE_CONSUMED.intValue()) {
                consumedEventDAO.save(info);
            }else if(info.getType() == MonitorEventInfo.TYPE_RECEIVED.intValue()){
                receivedEventDAO.save(info);
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<MonitorEventInfo> popEventInfos(int maxSize) {
        // at present, it didn't implement store and forward
        return null;
    }

    @Override
    public MonitorEventInfo popEventInfo() {
        return null;
    }

    @Override
    public void saveNodeInfo(NodeInfo nodeInfo) {

    }

    @Override
    public NodeInfo queryNodeInfo() {
        return null;
    }

    public ConsumedEventDAO getConsumedEventDAO() {
        return consumedEventDAO;
    }

    public void setConsumedEventDAO(ConsumedEventDAO consumedEventDAO) {
        this.consumedEventDAO = consumedEventDAO;
    }

    public ReceivedEventDAO getReceivedEventDAO() {
        return receivedEventDAO;
    }

    public void setReceivedEventDAO(ReceivedEventDAO receivedEventDAO) {
        this.receivedEventDAO = receivedEventDAO;
    }
}
