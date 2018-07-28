package eventcenter.monitor.client;

import com.alibaba.fastjson.JSONObject;
import eventcenter.monitor.*;
import eventcenter.monitor.AbstractControlMonitor;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * {@link InfoStorage}将以日志存储的方式进行保存，通过logger进行写入，写入的对象类型为{@link java.util.Map}
 * Created by liumingjian on 2017/2/15.
 */
public class LogControlMonitor extends AbstractControlMonitor {

    private final Logger logger = Logger.getLogger(this.getClass());

    @PostConstruct
    @Override
    public void startup() {
        infoForward = new LogInfoForward();
        super.startup();
    }

    @Override
    protected InfoStorage loadInfoStorage() {
        return new LogInfoStorage();
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

    class LogInfoStorage implements InfoStorage {

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
            for(MonitorEventInfo info : infos){
                pushEventInfo(info);
            }
        }

        @Override
        public void pushEventInfo(MonitorEventInfo info) {
            if(info.getException() != null){
                info.setExceptionMessage(info.getException().getMessage());
                StringWriter sw = new StringWriter();
                PrintWriter writer = new PrintWriter(sw);
                try {
                    info.getException().printStackTrace(writer);
                    info.setExceptionStack(sw.toString());
                }finally{
                    writer.close();
                }
                info.setException(null);
            }
            JSONObject json = (JSONObject) JSONObject.toJSON(info);
            logger.info(json);
        }

        @Override
        public List<MonitorEventInfo> popEventInfos(int maxSize) {
            return null;
        }

        @Override
        public MonitorEventInfo popEventInfo() {
            return null;
        }

        @Override
        public void saveNodeInfo(NodeInfo nodeInfo) {
            // Nothing to implement
        }

        @Override
        public NodeInfo queryNodeInfo() {
            return null;
        }
    }
}
