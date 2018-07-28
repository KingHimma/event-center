package eventcenter.builder.monitor.mysql;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.MonitorConfig;
import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.mysql.MySqlControlMonitor;
import eventcenter.monitor.mysql.MySqlEventFireFilter;
import eventcenter.monitor.mysql.MySqlSubscriberStartupFilter;

import javax.sql.DataSource;

/**
 *
 * @author liumingjian
 * @date 2017/9/14
 */
public class MysqlMonitorConfig extends MonitorConfig {
    private static final long serialVersionUID = 4711485326129011533L;

    protected DataSource dataSource;

    /**
     * 有关数据池的spring的beanId
     */
    protected String dataSourceBeanId;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected AbstractControlMonitor createControlMonitor(DefaultEventCenter eventCenter, boolean subscriber) {
        MySqlControlMonitor monitor = new MySqlControlMonitor();
        monitor.setControlMonitorDataSource(dataSource);
        return monitor;
    }

    @Override
    protected void loadFilters(AbstractControlMonitor controlMonitor, DefaultEventCenter eventCenter, boolean subscriber) {
        super.loadFilters(controlMonitor, eventCenter, subscriber);
        MySqlEventFireFilter mySqlEventFireFilter = new MySqlEventFireFilter();
        mySqlEventFireFilter.setControlMonitorDataSource(dataSource);
        mySqlEventFireFilter.startUp();
        eventCenter.getEcConfig().getModuleFilters().add(mySqlEventFireFilter);
        if(subscriber){
            MySqlSubscriberStartupFilter mySqlSubscriberStartupFilter = new MySqlSubscriberStartupFilter();
            mySqlSubscriberStartupFilter.setControlMonitorDataSource(dataSource);
            eventCenter.getEcConfig().getModuleFilters().add(mySqlSubscriberStartupFilter);
        }
    }

    public String getDataSourceBeanId() {
        return dataSourceBeanId;
    }

    public void setDataSourceBeanId(String dataSourceBeanId) {
        this.dataSourceBeanId = dataSourceBeanId;
    }
}
