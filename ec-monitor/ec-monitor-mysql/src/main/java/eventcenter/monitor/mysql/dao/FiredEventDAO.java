package eventcenter.monitor.mysql.dao;

import eventcenter.api.EventInfo;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 用于存储和查询firedEvent的数据
 * Created by liumingjian on 2017/4/6.
 */
public class FiredEventDAO extends BaseDateSuffixDAO {
    public FiredEventDAO() {
    }

    public FiredEventDAO(DataSource dataSource) {
        super(dataSource);
    }

    static final String TABLE_NAME = "fired_event";

    static final String SQL_CREATE_TABLE = "CREATE TABLE `%s` (\n" +
            "  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
            "  `event_id` varchar(128) NOT NULL ,\n" +
            "  `event_name` varchar(128) NOT NULL,\n" +
            "  `node_id` VARCHAR(128) NOT NULL,\n" +
            "  `mdc` varchar(128) DEFAULT NULL,\n" +
            "  `target` VARCHAR(256) DEFAULT NULL,\n" +
            "  `created` datetime NOT NULL,\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `event_id_idx` (`event_id`),\n" +
            "  KEY `event_name_idx` (`event_name`)\n" +
            ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";

    static final String SQL_INSERT = "insert into `%s` (`event_id`, `event_name`, `node_id`, `mdc`, `target`, `created`) values (?, ?, ?, ?, ?, ?)";

    @Override
    protected String getBuildTableSql(String tableName) {
        return String.format(SQL_CREATE_TABLE, buildTableName(tableName));
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public void save(String nodeId, Object target, EventInfo eventInfo){
        super.save(new IJdbcTemplateCallback() {
            @Override
            public void handle(JdbcTemplate jdbcTemplate, Object... args) {
                String nodeId = (String)args[0];
                Object target = args[1];
                EventInfo eventInfo1 = (EventInfo)args[2];
                jdbcTemplate.update(String.format(SQL_INSERT, buildTableName(getTableName())), eventInfo1.getId(), eventInfo1.getName(), nodeId, eventInfo1.getMdcValue(), (target != null) ? target.getClass().getName() : "null", new Date());
            }
        }, nodeId, target, eventInfo);
    }

    public void batchSave(String nodeId, List<FiredEventSaveDTO> dtos) {
        super.save(new IJdbcTemplateCallback() {
            @Override
            public void handle(JdbcTemplate jdbcTemplate, Object... args) {
                final String nodeId = (String) args[0];
                final List<FiredEventSaveDTO> dtos = (List<FiredEventSaveDTO>) args[1];
                jdbcTemplate.batchUpdate(String.format(SQL_INSERT, buildTableName(getTableName())), new BatchPreparedStatementSetter() {
                    FiredEventSaveDTO dto;

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        dto = dtos.get(i);
                        EventInfo eventInfo = dto.getEventInfo();
                        ps.setString(1, eventInfo.getId());
                        ps.setString(2, eventInfo.getName());
                        ps.setString(3, nodeId);
                        ps.setString(4, eventInfo.getMdcValue());
                        ps.setString(5, dto.getSourceClassName());
                        ps.setTimestamp(6, new Timestamp(dto.getTimestamp()));
                    }

                    @Override
                    public int getBatchSize() {
                        return dtos.size();
                    }
                });
            }
        }, nodeId, dtos);
    }

    public static class FiredEventSaveDTO {
        private String sourceClassName;

        private EventInfo eventInfo;

        private long timestamp;

        public String getSourceClassName() {
            return sourceClassName;
        }

        public void setSourceClassName(String sourceClassName) {
            this.sourceClassName = sourceClassName;
        }

        public EventInfo getEventInfo() {
            return eventInfo;
        }

        public void setEventInfo(EventInfo eventInfo) {
            this.eventInfo = eventInfo;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
