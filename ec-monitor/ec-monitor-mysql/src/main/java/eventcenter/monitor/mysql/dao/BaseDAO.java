package eventcenter.monitor.mysql.dao;

import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * 数据库的表名，必须使用'_'作为单词间的分隔符，表名全部为小写
 * Created by liumingjian on 2017/4/6.
 */
public abstract class BaseDAO {
    protected static final String SQL_SHOW_TABLES = "SHOW TABLES LIKE ?";

    protected static final String TABLE_NAME_SEPARATOR = "_";

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected volatile boolean isInit = false;

    public BaseDAO(){}

    public BaseDAO(DataSource dataSource){
        this.dataSource = dataSource;
    }

    /**
     * 初始化DAO，必要的参数,dataSource和tableName需要设置，首先检查表是否存在，如果不存在则会创建
     */
    @PostConstruct
    public void init(){
        if(isInit)
            return ;
        if(StringHelper.isEmpty(getTableName()))
            throw new IllegalArgumentException("please set parameter of tableName");
        if(null == dataSource)
            throw new IllegalArgumentException("please set parameter of dataSource");
        try {
            if (!checkTableExists(this.getTableName())) {
                createTable(this.getTableName());
            }
        }catch(Exception e){
            logger.error(e.getMessage() ,e);
        }
        isInit = true;
    }

    /**
     * 检查表是否存在
     * @param tableName
     * @return
     */
    protected boolean checkTableExists(String tableName){
        if(StringHelper.isEmpty(tableName))
            throw new IllegalArgumentException("please set tableName");
        final String tn = buildTableName(tableName);
        String result = getJdbcTemplate().queryForObject(SQL_SHOW_TABLES, String.class, tn);
        return StringHelper.equals(tn, result);
    }

    /**
     * 创建表，如果表已经存在了，则会
     * @param tableName
     */
    protected void createTable(String tableName){
        final String sql = getBuildTableSql(tableName);
        final long start = System.currentTimeMillis();
        getJdbcTemplate().execute(sql);
        logger.info("create table:" + sql + " success, took:" + (System.currentTimeMillis() - start) + " ms.");
    }

    /**
     * 获取DAO的建表语句，其中table name需要使用$tableName，作为占位符号，方法实现时，表明的获取应该使用 {@link #buildTableName(String)}方法
     * @return
     */
    protected abstract String getBuildTableSql(String tableName);

    protected abstract String getTableName();

    /**
     * 这个方法专门保存业务数据，其中当表不存在时，他会自动创建表，callback中处理具体的保存逻辑
     * @param callback
     * @param args
     */
    protected void save(IJdbcTemplateCallback callback, Object... args){
        save0(0, callback, args);
    }

    private boolean save0(int retry, IJdbcTemplateCallback callback, Object... args){
        // 只允许重试三次
        if(retry > 2)
            return false;
        try {
            callback.handle(getJdbcTemplate(), args);
            return true;
        }catch(BadSqlGrammarException e){
            if(e.getSQLException() == null || !e.getSQLException().getMessage().contains("doesn't exist"))
                throw e;
            ++retry;
            createTable0(retry, callback, args);
            return save0(retry, callback, args);
        }
    }

    private void createTable0(int retry, IJdbcTemplateCallback callback, Object... args){
        try {
            createTable(getTableName());
        }catch(BadSqlGrammarException e){
            if(e.getSQLException() == null || !e.getSQLException().getMessage().contains("already exists"))
                throw e;
            if(!save0(++retry, callback, args))
                throw e;
        }
    }

    /**
     * 这个方法主要获取表名，由于有些业务表需要使用日期作为后缀，所以实现{@link #getBuildTableSql(String)}方法时，应该调用这个方法获取表名
     * @param tableNamePrefix
     * @return
     */
    protected abstract String buildTableName(String tableNamePrefix);

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 如果没有设置dataSource参数，则会报参数错误异常
     * @return
     */
    protected JdbcTemplate getJdbcTemplate(){
        if(null == dataSource)
            throw new IllegalArgumentException("please set parameter of dataSource");

        if(null == jdbcTemplate){
            synchronized (this){
                if(null != jdbcTemplate)
                    return jdbcTemplate;
                jdbcTemplate = new JdbcTemplate(this.dataSource);
            }
        }
        return jdbcTemplate;
    }
}
