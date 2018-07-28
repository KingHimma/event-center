package eventcenter.monitor.mysql;

import eventcenter.monitor.mysql.dao.SubscriberInfoDAO;
import eventcenter.remote.SubscriberGroup;
import eventcenter.remote.subscriber.SubscriberStartupFilter;
import eventcenter.monitor.mysql.dao.SubscriberInfoDAO;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author liumingjian
 * @date 2017/4/11
 */
public class MySqlSubscriberStartupFilter implements SubscriberStartupFilter {

    DataSource controlMonitorDataSource;

    SubscriberInfoDAO subscriberInfoDAO;

    final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void onStartup(Map<String, SubscriberGroup> subscriberGroups) {
        if(null == subscriberGroups)
            return ;
        try{
            Collection<SubscriberGroup> groups = subscriberGroups.values();
            for(SubscriberGroup group : groups) {
                getSubscriberInfoDAO().save(group);
            }
        }catch(Exception e){
            logger.error(e.getMessage() ,e);
        }
    }

    SubscriberInfoDAO getSubscriberInfoDAO(){
        if(null != subscriberInfoDAO){
            return subscriberInfoDAO;
        }
        subscriberInfoDAO = new SubscriberInfoDAO(controlMonitorDataSource);
        return subscriberInfoDAO;
    }

    public DataSource getControlMonitorDataSource() {
        return controlMonitorDataSource;
    }

    public void setControlMonitorDataSource(DataSource controlMonitorDataSource) {
        this.controlMonitorDataSource = controlMonitorDataSource;
    }
}
