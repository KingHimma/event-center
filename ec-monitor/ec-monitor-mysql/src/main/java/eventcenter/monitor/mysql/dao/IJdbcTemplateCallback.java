package eventcenter.monitor.mysql.dao;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 调用{@link BaseDAO}的save方法时，需要传递这个回调进去，save方法封装了一套调用模板，可以处理表不存在时，自动创建的相关逻辑
 * Created by liumingjian on 2017/4/6.
 */
public interface IJdbcTemplateCallback {

    public void handle(JdbcTemplate jdbcTemplate, Object... args);
}
