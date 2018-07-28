package eventcenter.monitor.mysql;

import com.alibaba.fastjson.JSONObject;
import eventcenter.monitor.*;
import eventcenter.monitor.mysql.dao.ConsumedEventDAO;
import eventcenter.monitor.mysql.dao.NodeInfoDAO;
import eventcenter.monitor.mysql.dao.ReceivedEventDAO;
import eventcenter.monitor.mysql.dao.ConsumedEventDAO;

import javax.sql.DataSource;
import java.util.List;

/**
 *
 * @author liumingjian
 * @date 2017/4/6
 */
public class MySqlControlMonitor extends AbstractControlMonitor {

    DataSource controlMonitorDataSource;

    NodeInfoDAO nodeInfoDAO;

    ConsumedEventDAO consumedEventDAO;

    ReceivedEventDAO receivedEventDAO;

    @Override
    public void startup() {
        if(open || delayInitLock.isLock()){
            return ;
        }
        if(null == controlMonitorDataSource)
            throw new IllegalArgumentException("please set parameter of controlMonitorDataSource");
        nodeInfoDAO = new NodeInfoDAO(controlMonitorDataSource);
        consumedEventDAO = new ConsumedEventDAO(controlMonitorDataSource);
        receivedEventDAO = new ReceivedEventDAO(controlMonitorDataSource);
        init();
        infoForward = new LogInfoForward();
        openHeartBeat();
    }

    @Override
    protected void init() {
        super.init();
        // save node info
        try {
            nodeInfoDAO.init();
            nodeInfoDAO.save(nodeInfo);
        }catch(Exception e){
            logger.error(e.getMessage() ,e);
        }
    }

    @Override
    protected InfoStorage loadInfoStorage() {
        MysqlInfoStorage storage = new MysqlInfoStorage();
        storage.setConsumedEventDAO(consumedEventDAO);
        storage.setReceivedEventDAO(receivedEventDAO);
        return storage;
    }

    public DataSource getControlMonitorDataSource() {
        return controlMonitorDataSource;
    }

    public void setControlMonitorDataSource(DataSource controlMonitorDataSource) {
        this.controlMonitorDataSource = controlMonitorDataSource;
    }

    class LogInfoForward implements InfoForward {

        @Override
        public void forwardNodeInfo(NodeInfo info) {
            logger.info(JSONObject.toJSON(info));
        }

        @Override
        public void forwardEventInfo(List<MonitorEventInfo> infos) {
            // Nothing to implement
        }
    }
}
