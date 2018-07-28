package eventcenter.monitor.mysql.dao;

import com.mysql.jdbc.Driver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Created by liumingjian on 2017/4/7.
 */
public class DAOTestBase<T extends BaseDAO> {

    protected SimpleDriverDataSource dataSource;

    protected T init(Class<T> type) throws Exception {
        // TODO 请自行设置数据库的用户名和密码
        dataSource = new SimpleDriverDataSource(new Driver(), "jdbc:mysql://127.0.0.1:3306/erp?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull", "eventcenter", "eventcenter");
        T dao = type.newInstance();
        dao.setDataSource(dataSource);
        return dao;
    }

    protected void destroy(){

    }
}
