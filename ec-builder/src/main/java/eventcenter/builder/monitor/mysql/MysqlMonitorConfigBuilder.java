package eventcenter.builder.monitor.mysql;

import javax.sql.DataSource;

/**
 *
 * @author liumingjian
 * @date 2017/9/14
 */
public class MysqlMonitorConfigBuilder {

    MysqlMonitorConfig config;

    protected MysqlMonitorConfig getConfig(){
        if(null == config){
            config = new MysqlMonitorConfig();
        }
        return config;
    }

    public MysqlMonitorConfigBuilder saveEventData(Boolean saveEventData){
        getConfig().setSaveEventData(saveEventData);
        return this;
    }

    public MysqlMonitorConfigBuilder heartbeatInterval(Long heartbeatInterval){
        getConfig().setHeartbeatInterval(heartbeatInterval);
        return this;
    }

    public MysqlMonitorConfigBuilder nodeName(String nodeName){
        getConfig().setNodeName(nodeName);
        return this;
    }

    public MysqlMonitorConfigBuilder dataSource(DataSource dataSource){
        getConfig().setDataSource(dataSource);
        return this;
    }

    public MysqlMonitorConfigBuilder dataSourceBeanId(String dataSourceBeanId){
        getConfig().setDataSourceBeanId(dataSourceBeanId);
        return this;
    }

    public MysqlMonitorConfig build(){
        return getConfig();
    }
}
