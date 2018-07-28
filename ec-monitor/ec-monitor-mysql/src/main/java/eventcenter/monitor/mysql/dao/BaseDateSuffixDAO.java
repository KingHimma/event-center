package eventcenter.monitor.mysql.dao;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 表名基于日期创建
 * Created by liumingjian on 2017/4/6.
 */
public abstract class BaseDateSuffixDAO extends BaseDAO {

    public BaseDateSuffixDAO() {
    }

    public BaseDateSuffixDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String buildTableName(String tableNamePrefix) {
        return new StringBuilder(tableNamePrefix).append(TABLE_NAME_SEPARATOR).append(getCurrentDateSuffix()).toString();
    }

    private String getCurrentDateSuffix(){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        return sdf.format(now);
    }
}
