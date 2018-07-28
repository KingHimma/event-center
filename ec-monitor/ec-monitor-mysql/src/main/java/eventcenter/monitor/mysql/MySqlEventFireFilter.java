package eventcenter.monitor.mysql;

import eventcenter.api.*;
import eventcenter.api.appcache.IdentifyContext;
import eventcenter.leveldb.LevelDBPersistenceAdapter;
import eventcenter.leveldb.LevelDBQueue;
import eventcenter.leveldb.QueueMiddleComponent;
import eventcenter.monitor.mysql.dao.FiredEventDAO;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 2017/4/6.
 */
public class MySqlEventFireFilter implements EventFireFilter {
    private final Logger logger = Logger.getLogger(this.getClass());

    DataSource controlMonitorDataSource;

    FiredEventDAO firedEventDAO;

    private String nodeId;

    private LevelDBPersistenceAdapter adapter;

    private QueueMiddleComponent queueMiddle;

    private LevelDBQueue queue;

    private int batchSaveNum = 10;

    private volatile boolean isOpen = false;

    @PostConstruct
    public void startUp() {
        adapter = new LevelDBPersistenceAdapter();
        queueMiddle = new QueueMiddleComponent(adapter);
        File dir = new File(System.getProperty("user.home") + File.separator + ".monitorleveldb");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        adapter.setDirPath(dir);
        adapter.setName("mysqlEventFireFilter");

        queue = new LevelDBQueue(queueMiddle);
        try {
            queue.open();
            isOpen = true;
        } catch (IOException e) {
            logger.error("startup mySqlEventFireFilter failure:" + e.getMessage(), e);
        }

        new BatchSaveEventFireInfoToMySqlThread().start();
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (null != queue) {
            queue.close();
        }
    }

    @Override
    public void onFired(Object target, EventInfo eventInfo, Object result) {
        CommonEventSource commonEventSource = new CommonEventSource(target, eventInfo.getId(), eventInfo.getName(), null, null, eventInfo.getMdcValue());
        commonEventSource.setSourceInfo(eventInfo);
        commonEventSource.setSourceClassName(null != target ? target.getClass().getName() : "null");
        queue.offer(commonEventSource);
    }

    private String getNodeId(){
        if(StringHelper.isNotEmpty(nodeId))
            return nodeId;
        try {
            nodeId = IdentifyContext.getId();
        } catch (Exception e) {
            logger.error(e.getMessage());
            nodeId = "";
        }
        return nodeId;
    }

    FiredEventDAO getFiredEventDAO() {
        if(null != firedEventDAO)
            return firedEventDAO;
        if(null == controlMonitorDataSource)
            throw new IllegalArgumentException("please set parameter of controlMonitorDataSource");
        firedEventDAO = new FiredEventDAO(controlMonitorDataSource);
        return firedEventDAO;
    }

    public DataSource getControlMonitorDataSource() {
        return controlMonitorDataSource;
    }

    public void setControlMonitorDataSource(DataSource controlMonitorDataSource) {
        this.controlMonitorDataSource = controlMonitorDataSource;
    }

    public int getBatchSaveNum() {
        return batchSaveNum;
    }

    public void setBatchSaveNum(int batchSaveNum) {
        this.batchSaveNum = batchSaveNum;
    }

    private class BatchSaveEventFireInfoToMySqlThread extends Thread {
        public BatchSaveEventFireInfoToMySqlThread() {
            super("batchsave-eventfireinfo-to-mysql-thread");
        }

        @Override
        public void run() {
            List<EventSourceBase> list = new ArrayList<EventSourceBase>(batchSaveNum);
            while (isOpen) {
                EventSourceBase eventSourceBase = queue.transfer(1000);
                String nid = getNodeId();
                if (null != nid && null != eventSourceBase) {
                    list.add(eventSourceBase);
                }
                if (batchSaveNum == list.size()) {
                    try {
                        List<FiredEventDAO.FiredEventSaveDTO> dtos = new ArrayList<FiredEventDAO.FiredEventSaveDTO>(batchSaveNum);
                        for (EventSourceBase sourceBase : list) {
                            FiredEventDAO.FiredEventSaveDTO dto = new FiredEventDAO.FiredEventSaveDTO();
                            dto.setSourceClassName(sourceBase.getSourceClassName());
                            dto.setEventInfo((EventInfo) sourceBase.getSourceInfo());
                            dto.setTimestamp(sourceBase.getTimestamp());
                            dtos.add(dto);
                        }
                        getFiredEventDAO().batchSave(nid, dtos);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        list.clear();
                    }
                }
            }
        }
    }
}
