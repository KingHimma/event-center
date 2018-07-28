package eventcenter.leveldb.saf;

import eventcenter.api.async.EventQueue;
import eventcenter.leveldb.LevelDBPersistenceAdapter;
import eventcenter.leveldb.LevelDBQueue;
import eventcenter.leveldb.QueueMiddleComponent;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import eventcenter.remote.saf.EventForward;
import eventcenter.remote.saf.StoreAndForwardPolicy;
import eventcenter.remote.saf.simple.SimpleEventForward;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * 使用levelDB执行离线推送机制
 * @author JackyLIU
 *
 */
public class LevelDBStoreAndForwardPolicy implements StoreAndForwardPolicy {

	private boolean isStoreOnSendFail = true;
	
	/**
	 * 检查远程端的健康值的间隔，默认为60000毫秒
	 */
	private Long checkInterval;
	
	/**
	 * 是否初始化levelDB
	 */
	private volatile boolean openLevelDB = false;
	
	private LevelDBPersistenceAdapter adapter;
	
	private String path;
	
	private Integer readLimitSize;
	
	private String levelDBName;
	
	private Long houseKeepingInterval;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public boolean storeOnSendFail() {
		return isStoreOnSendFail;
	}

	public boolean isStoreOnSendFail() {
		return isStoreOnSendFail;
	}

	public void setStoreOnSendFail(boolean isStoreOnSendFail) {
		this.isStoreOnSendFail = isStoreOnSendFail;
	}

	public Long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(Long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getReadLimitSize() {
		return readLimitSize;
	}

	public void setReadLimitSize(Integer readLimitSize) {
		this.readLimitSize = readLimitSize;
	}

	public String getLevelDBName() {
		return levelDBName;
	}

	public void setLevelDBName(String levelDBName) {
		this.levelDBName = levelDBName;
	}

	public Long getHouseKeepingInterval() {
		return houseKeepingInterval;
	}

	public void setHouseKeepingInterval(Long houseKeepingInterval) {
		this.houseKeepingInterval = houseKeepingInterval;
	}

	@Override
	public synchronized EventQueue createEventQueue(String groupName) {
		if(StringHelper.isEmpty(groupName)){
			throw new IllegalArgumentException("please set group name for publisher group");
		}
		if(!openLevelDB){
			try {
				init();
			} catch (IOException e) {
				throw new IllegalArgumentException("start up leveldb saf policy failure:" + e.getMessage());
			}
		}
		QueueMiddleComponent queueMiddle = new QueueMiddleComponent(adapter, groupName);
		LevelDBQueue queue = new LevelDBQueue(queueMiddle);
		LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queue);
		if(null != houseKeepingInterval)
			strategy.setCheckInterval(houseKeepingInterval);
		if(null != readLimitSize)
			strategy.setReadLimitSize(readLimitSize);

		queue.setHouseKeepingStrategy(strategy);
		try {
			queue.open();
		} catch (IOException e) {
			throw new IllegalArgumentException("start up leveldb saf policy failure:" + e.getMessage());
		}
		if(logger.isDebugEnabled()){
			logger.debug(new StringBuilder("create saf queue:leveldb^").append(groupName).append(" success."));
		}
		return queue;
	}
	
	private void init() throws IOException {
		adapter = new LevelDBPersistenceAdapter();
		File dir = new File(System.getProperty("user.home") + File.separator + ".safecdb");
		if(path != null){
			dir = new File(new StringBuilder(path).append(File.separator).append(".safecdb").toString());
		}
		adapter.setDirPath(dir);
		adapter.setName(levelDBName);
		adapter.open();
		openLevelDB = true;
		logger.info(new StringBuilder("start leveldb saf success, path:").append(dir.getPath()));
	}

	@Override
	public EventForward createEventForward() {
		SimpleEventForward forward = new SimpleEventForward(isStoreOnSendFail);
		if(null != checkInterval)
			forward.setCheckInterval(checkInterval);

		return forward;
	}

}
