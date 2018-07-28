package eventcenter.monitor.mysql.dao;

import eventcenter.api.appcache.IdentifyContext;
import eventcenter.remote.SubscriberGroup;
import eventcenter.remote.utils.StringHelper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;

/**
 * Created by liumingjian on 2017/4/7.
 */
public class SubscriberInfoDAO extends BaseDAO {

    public SubscriberInfoDAO() {
        super();
    }

    public SubscriberInfoDAO(DataSource dataSource) {
        super(dataSource);
    }

    static final String TABLE_NAME = "sub_info";

    private String nodeId;

    static final String SQL_CREATE_TABLE = "CREATE TABLE `sub_info` (\n" +
            "\t`node_id` VARCHAR(128) NOT NULL,\n" +
            "\t`group` VARCHAR(128) NOT NULL,\n" +
            "\t`sub_event` VARCHAR(2048) NOT NULL,\n" +
            "\t`created` datetime NOT NULL,\n" +
            "\tPRIMARY KEY (`node_id`, `group`)" +
            ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";

    static final String SQL_INSERT = "insert into `sub_info` (`node_id`, `group`, `sub_event`, `created`) values (?, ?, ?, ?) on duplicate key update `sub_event` = ?";

    @Override
    protected String getBuildTableSql(String tableName) {
        return SQL_CREATE_TABLE;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String buildTableName(String tableNamePrefix) {
        return tableNamePrefix;
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

    /**
     * 保存节点信息，不包含节点的容器统计数据
     * @param subscriberGroup
     */
    public void save(SubscriberGroup subscriberGroup){
        super.save(new IJdbcTemplateCallback() {
            @Override
            public void handle(JdbcTemplate jdbcTemplate, Object... args) {
                SubscriberGroup group = (SubscriberGroup)args[0];
                jdbcTemplate.update(SQL_INSERT, getNodeId(), group.getGroupName()==null?"default":group.getGroupName(), group.getRemoteEvents(), new Date(), group.getRemoteEvents());
            }
        }, subscriberGroup);
    }
}
