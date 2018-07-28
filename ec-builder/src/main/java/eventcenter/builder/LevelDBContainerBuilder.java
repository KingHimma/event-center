package eventcenter.builder;

import eventcenter.leveldb.LevelDBContainerFactory;

/**
 * 构建{@link LevelDBContainerFactory}工厂类
 * Created by liumingjian on 2017/9/1.
 */
public class LevelDBContainerBuilder {

    LevelDBContainerFactory levelDBContainerFactory;

    LevelDBContainerFactory getFactory(){
        if(null == levelDBContainerFactory){
            levelDBContainerFactory = new LevelDBContainerFactory();
        }
        return levelDBContainerFactory;
    }

    /**
     * leveldb的队列使用的是游标的方式管理数据，游标包括读、写和删，删除的游标是根据时间间隔扫描以及读取的阀值触发，
     * checkInterval是删除游标的间隔，单位毫秒，默认是10000毫秒
     * @param checkInterval
     * @return
     */
    public LevelDBContainerBuilder checkInterval(Long checkInterval){
        getFactory().setCheckInterval(checkInterval);
        return this;
    }

    /**
     * path为leveldb的数据库文件路径，默认为 ${user.home}/eventcenterdb路径，可不配置
     * @param path
     * @return
     */
    public LevelDBContainerBuilder path(String path){
        getFactory().setPath(path);
        return this;
    }

    /**
     * 运行容器的线程池的核心容量，默认为当前服务器的CPU内核数
     * @param corePoolSize
     * @return
     */
    public LevelDBContainerBuilder corePoolSize(Integer corePoolSize){
        getFactory().setCorePoolSize(corePoolSize);
        return this;
    }

    /**
     * 运行容器的线程池的最大核心容量，默认为当前服务器的CPU内核数的两倍
     * @param maximumPoolSize
     * @return
     */
    public LevelDBContainerBuilder maximumPoolSize(Integer maximumPoolSize){
        getFactory().setMaximumPoolSize(maximumPoolSize);
        return this;
    }

    /**
     * leveldb的数据库名称，如果当前path下存在多个数据库实例，可以配置这个名称进行区分，leveldb会根据名称自动添加子目录，这个配置一般不用设置
     * @param levelDBName
     * @return
     */
    public LevelDBContainerBuilder levelDBName(String levelDBName){
        getFactory().setLevelDBName(levelDBName);
        return this;
    }

    /**
     * 这个是读取的阀值，默认为1000条消息
     * @param readLimitSize
     * @return
     */
    public LevelDBContainerBuilder readLimitSize(Integer readLimitSize){
        getFactory().setReadLimitSize(readLimitSize);
        return this;
    }

    /**
     * 开启事务性的队列读取模式，当从队列取出元素时，将会记录到事务队列中，一旦事件没有执行完，下次启动服务将会重新执行取出队列并且未执行的完的事件。
     * @param openTxn
     * @return
     */
    public LevelDBContainerBuilder openTxn(Boolean openTxn){
        getFactory().setOpenTxn(openTxn);
        return this;
    }

    /**
     * 线程池空闲线程的生存时间
     * @param keepAliveTime
     * @return
     */
    public LevelDBContainerBuilder keepAliveTime(Integer keepAliveTime){
        getFactory().setKeepAliveTime(keepAliveTime);
        return this;
    }

    /**
     * it would calculate blocking queue capacity by maximumPoolSize * factory
     * @param blockingQueueFactory
     * @return
     */
    public LevelDBContainerBuilder blockingQueueFactor(Integer blockingQueueFactory){
        getFactory().setBlockingQueueFactor(blockingQueueFactory);
        return this;
    }

    /**
     * 遍历循环队列中的元素间隔时间，单位毫秒
     * @param loopQueueInterval
     * @return
     */
    public LevelDBContainerBuilder loopQueueInterval(Long loopQueueInterval){
        getFactory().setLoopQueueInterval(loopQueueInterval);
        return this;
    }

    /**
     * 是否开启leveldb中间件中的日志，如果开启了，日志的logger为eventcenter.leveldb.LevelDBPersistenceAdapter
     * @param openLevelDbLog
     * @return
     */
    public LevelDBContainerBuilder openLevelDbLog(Boolean openLevelDbLog){
        getFactory().setOpenLevelDbLog(openLevelDbLog);
        return this;
    }

    public LevelDBContainerFactory build(){
        return getFactory();
    }
}
