package eventcenter.monitor.mysql.dao;

import eventcenter.monitor.NodeInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;

/**
 * Created by liumingjian on 2017/4/7.
 */
public class NodeInfoDAO extends BaseDAO {

    public NodeInfoDAO() {
        super();
    }

    public NodeInfoDAO(DataSource dataSource) {
        super(dataSource);
    }

    static final String TABLE_NAME = "node_info";

    static final String SQL_CREATE_TABLE = "CREATE TABLE `node_info` (\n" +
            "\t`id` VARCHAR(128) PRIMARY KEY,\n" +
            "\t`group` VARCHAR(64) NOT NULL,\n" +
            "\t`name` VARCHAR(64) NOT NULL,\n" +
            "\t`host` VARCHAR(128) NOT NULL,\n" +
            "\t`created` datetime NOT NULL,\n" +
            "\t`start` datetime,\n" +
            "\tKEY `group_name_idx` (`group`, `name`)\n" +
            ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";

    static final String SQL_INSERT = "insert into `node_info` (`id`, `group`, `name`, `host`, `created`, `start`) values (?, ?, ?, ?, ?, ?) on duplicate key update `group` = ?, `name` = ?, `host` = ?, `start` = ?";

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

    /**
     * 保存节点信息，不包含节点的容器统计数据
     * @param nodeInfo
     */
    public void save(NodeInfo nodeInfo){
        if(nodeInfo.getStart() == null)
            nodeInfo.setStart(new Date());
        super.save(new IJdbcTemplateCallback() {
            @Override
            public void handle(JdbcTemplate jdbcTemplate, Object... args) {
                NodeInfo nodeInfo1 = (NodeInfo)args[0];
                jdbcTemplate.update(SQL_INSERT, nodeInfo1.getId(), nodeInfo1.getGroup(), nodeInfo1.getName(), nodeInfo1.getHost(), new Date(), nodeInfo1.getStart(), nodeInfo1.getGroup(), nodeInfo1.getName(), nodeInfo1.getHost(), nodeInfo1.getStart());
            }
        }, nodeInfo);
    }
}
