package eventcenter.leveldb;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.async.QueueEventContainerFactory;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import eventcenter.leveldb.tx.TransactionConfig;

import java.io.File;

/**
 * 创建LevelDB的实现方式的队列容器，能够持久化到磁盘中的队列
 * @author JackyLIU
 *
 */
public class LevelDBContainerFactory implements
        QueueEventContainerFactory {

	private LevelDBPersistenceAdapter adapter;
	
	private QueueMiddleComponent queueMiddle;
	
	private LevelDBQueue queue;
	
	private LevelDBContainer container;

	/**
	 * 是否开启事务
	 */
	private boolean openTxn = false;

	private TransactionConfig transactionConfig;
	
	private String path;
	
	private Integer corePoolSize = Runtime.getRuntime().availableProcessors();
	
	private Integer maximumPoolSize;
	
	private Integer keepAliveTime;
	
	private Integer blockingQueueFactor;
	
	private Integer readLimitSize;
	
	private Long checkInterval;

	private Long loopQueueInterval;

	private String levelDBName;

	private Boolean openLevelDbLog;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(Integer corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public Integer getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(Integer maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public Integer getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(Integer keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public Integer getBlockingQueueFactor() {
		return blockingQueueFactor;
	}

	public void setBlockingQueueFactor(Integer blockingQueueFactor) {
		this.blockingQueueFactor = blockingQueueFactor;
	}

	public Integer getReadLimitSize() {
		return readLimitSize;
	}

	public void setReadLimitSize(Integer readLimitSize) {
		this.readLimitSize = readLimitSize;
	}

	public Long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(Long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public String getLevelDBName() {
		return levelDBName;
	}

	public void setLevelDBName(String levelDBName) {
		this.levelDBName = levelDBName;
	}

	public Boolean getOpenLevelDbLog() {
		return openLevelDbLog;
	}

	public void setOpenLevelDbLog(Boolean openLevelDbLog) {
		this.openLevelDbLog = openLevelDbLog;
	}

	public Long getLoopQueueInterval() {
		return loopQueueInterval;
	}

	public void setLoopQueueInterval(Long loopQueueInterval) {
		this.loopQueueInterval = loopQueueInterval;
	}

	public boolean isOpenTxn() {
		return openTxn;
	}

	public void setOpenTxn(boolean openTxn) {
		this.openTxn = openTxn;
	}

	/**
	 * 是否需要支持事务，开启事务对于读取事件队列有一定的开销，但是可以保证事件数据的一致性
	 * @return
	 */
	public TransactionConfig getTransactionConfig() {
		return transactionConfig;
	}

	/**
	 * 是否需要支持事务，开启事务对于读取事件队列有一定的开销，但是可以保证事件数据的一致性
	 * @param transactionConfig
	 */
	public void setTransactionConfig(TransactionConfig transactionConfig) {
		this.transactionConfig = transactionConfig;
	}

	@Override
	public QueueEventContainer createContainer(EventCenterConfig config) {
		if(null != container) {
			return container;
		}
		adapter = new LevelDBPersistenceAdapter();
		adapter.setOpenLogger(openLevelDbLog);
		queueMiddle = new QueueMiddleComponent(adapter);
		File dir = new File(System.getProperty("user.home") + File.separator + "eventcenterdb");
		if(null != path && !"".equals(path)){
			dir = new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}
		}
		adapter.setDirPath(dir);
		adapter.setName(levelDBName);
		
		queue = new LevelDBQueue(queueMiddle);
		LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queue);
		if(null != checkInterval) {
			strategy.setCheckInterval(checkInterval);
		}
		if(null != readLimitSize) {
			strategy.setReadLimitSize(readLimitSize);
		}
		queue.setHouseKeepingStrategy(strategy);
		
		container = new LevelDBContainer(config, queue);
		if(null != corePoolSize) {
			container.setCorePoolSize(corePoolSize);
		}
		if(null != maximumPoolSize) {
			container.setMaximumPoolSize(maximumPoolSize);
		}
		if(null != keepAliveTime) {
			container.setKeepAliveTime(keepAliveTime);
		}
		if(null != blockingQueueFactor) {
			container.setBlockingQueueFactor(blockingQueueFactor);
		}
		if(null != loopQueueInterval) {
			container.setLoopQueueInterval(loopQueueInterval);
		}

		if(null != transactionConfig) {
			container.setTransactionConfig(transactionConfig);
			queue.setTransactionConfig(transactionConfig);
		}else if(openTxn){
			transactionConfig = new TransactionConfig();
			container.setTransactionConfig(transactionConfig);
			queue.setTransactionConfig(transactionConfig);
		}
		return container;
	}

}
