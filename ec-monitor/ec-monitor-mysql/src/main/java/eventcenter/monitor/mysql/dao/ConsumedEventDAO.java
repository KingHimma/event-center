package eventcenter.monitor.mysql.dao;

import eventcenter.monitor.MonitorEventInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 通过EventListener执行的事件的回执数据
 * Created by liumingjian on 2017/4/10.
 */
public class ConsumedEventDAO extends BaseDateSuffixDAO {

    public ConsumedEventDAO() {
    }

    public ConsumedEventDAO(DataSource dataSource) {
        super(dataSource);
    }

    static final String TABLE_NAME = "consumed_event";

    static final String SQL_CREATE_TABLE = "CREATE TABLE `%s` (\n" +
            " `id` BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
            " `node_id` VARCHAR(128) NOT NULL,\n" +
            " `event_id` varchar(128) NOT NULL ,\n" +
            " `event_name` varchar(128) NOT NULL,\n" +
            " `from_node_id` varchar(128) DEFAULT NULL,\n" +
            " `listener` varchar(256) DEFAULT NULL,\n" +
            " `delay` INT(10) DEFAULT 0,\n" +
            " `took` INT(10) DEFAULT 0,\n" +
            " `suc` TINYINT(4) DEFAULT 1,\n" +
            " `mdc` varchar(128) DEFAULT '-1',\n" +
            " `consumed` datetime NOT NULL,\n" +
            " `created` datetime NOT NULL,\n" +
            " PRIMARY KEY (`id`),\n" +
            " KEY `event_id_idx` (`event_id`),\n" +
            " KEY `event_name_idx` (`event_name`),\n" +
            " KEY `node_id_idx` (`node_id`),\n" +
            " KEY `from_node_id_idx` (`from_node_id`)\n" +
            ") ENGINE=InnoDB  DEFAULT CHARSET=utf8";

    static final String SQL_INSERT = "insert into `%s` (`node_id`, `event_id`, `event_name`, `from_node_id`, `listener`, `delay`, `took`, `suc`, `mdc`, `consumed`, `created`) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    protected String getBuildTableSql(String tableName) {
        return String.format(SQL_CREATE_TABLE, buildTableName(tableName));
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public void save(MonitorEventInfo info){
        super.save(new IJdbcTemplateCallback() {
            @Override
            public void handle(JdbcTemplate jdbcTemplate, Object... args) {
                MonitorEventInfo info1 = (MonitorEventInfo)args[0];
                jdbcTemplate.update(String.format(SQL_INSERT, buildTableName(TABLE_NAME)), info1.getNodeId(), info1.getEventId(), info1.getEventName(), info1.getFromNodeId(), info1.getListenerClazz(), info1.getDelay(), info1.getTook(), info1.isSuccess()?"1":"0", info1.getMdcValue(), info1.getConsumed(), info1.getCreated());
            }
        }, info);
    }
}
