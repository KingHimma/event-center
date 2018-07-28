package eventcenter.leveldb;

import eventcenter.api.EventSourceBase;
import eventcenter.api.utils.IdWorker;
import eventcenter.api.utils.SerializeUtils;
import org.apache.log4j.Logger;
import org.iq80.leveldb.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * 使用LevelDB数据库封装持久化适配器
 * 
 * @author JackyLIU
 *
 */
public class LevelDBPersistenceAdapter implements Closeable{

	private String name;

	/**
	 * 数据库目录
	 */
	private File dirPath;

	private Integer blockRestartInterval = 16;

	private Integer blockSize = 4*1024;

	private Long cacheSize = 1024 * 1024 * 256L;

	private Boolean useSnappyCompression = true;

	private Integer maxOpenFiles = 1000;

	private Boolean paranoidChecks = false;

	private Boolean verifyChecksums = false;

	private Integer writeBufferSize = 1024*1024*6;	// 6M

	private Boolean openLogger;

	private Integer batchDeleteSize = 1000;

	protected DB db;
	
	protected final IdWorker idWorker;
	
	protected final Logger logger = Logger.getLogger(this.getClass());

	public LevelDBPersistenceAdapter(){
		idWorker = new IdWorker(1);
	}
	
	public File getDirPath() {
		if (null == dirPath) {
			dirPath = new File("eslogs");
		}
		return dirPath;
	}

	public void setDirPath(File dirPath) {
		this.dirPath = dirPath;
	}

	public Integer getBlockRestartInterval() {
		return blockRestartInterval;
	}

	public void setBlockRestartInterval(Integer blockRestartInterval) {
		this.blockRestartInterval = blockRestartInterval;
	}

	public Integer getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(Integer blockSize) {
		this.blockSize = blockSize;
	}

	public Long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Long cacheSize) {
		this.cacheSize = cacheSize;
	}

	public Boolean getUseSnappyCompression() {
		return useSnappyCompression;
	}

	public void setUseSnappyCompression(Boolean useSnappyCompression) {
		this.useSnappyCompression = useSnappyCompression;
	}

	public Integer getMaxOpenFiles() {
		return maxOpenFiles;
	}

	public void setMaxOpenFiles(Integer maxOpenFiles) {
		this.maxOpenFiles = maxOpenFiles;
	}

	public Boolean getParanoidChecks() {
		return paranoidChecks;
	}

	public void setParanoidChecks(Boolean paranoidChecks) {
		this.paranoidChecks = paranoidChecks;
	}

	public Boolean getVerifyChecksums() {
		return verifyChecksums;
	}

	public void setVerifyChecksums(Boolean verifyChecksums) {
		this.verifyChecksums = verifyChecksums;
	}

	public Integer getWriteBufferSize() {
		return writeBufferSize;
	}

	public void setWriteBufferSize(Integer writeBufferSize) {
		this.writeBufferSize = writeBufferSize;
	}

	public Integer getBatchDeleteSize() {
		return batchDeleteSize;
	}

	public void setBatchDeleteSize(Integer batchDeleteSize) {
		this.batchDeleteSize = batchDeleteSize;
	}

	protected Options createOptions() {
		Options options = new Options();
		options.createIfMissing(true);
		if (null != blockRestartInterval)
			options.blockRestartInterval(blockRestartInterval);
		if (null != blockSize)
			options.blockSize(blockSize);
		if (null != cacheSize)
			options.cacheSize(cacheSize);
		if (null != useSnappyCompression && useSnappyCompression.booleanValue())
			options.compressionType(CompressionType.SNAPPY);
		if (null != maxOpenFiles)
			options.maxOpenFiles(maxOpenFiles);
		if (null != paranoidChecks)
			options.paranoidChecks(paranoidChecks);
		if (null != verifyChecksums)
			options.verifyChecksums(verifyChecksums);
		if (null != writeBufferSize)
			options.writeBufferSize(writeBufferSize);
		if(null != openLogger && openLogger)
			options.logger(new org.iq80.leveldb.Logger() {
				@Override
				public void log(String message) {
					if(logger.isDebugEnabled()){
						logger.debug(message);
					}
				}
			});
		return options;
	}

	public synchronized void open() throws IOException {
		if(null != db)
			return ;
		Options options = createOptions();
		File dirPath = getDirPath();
		db = factory.open(dirPath, options);
		if(logger.isDebugEnabled()){
			logger.debug("open leveldb success, path:" + dirPath.getCanonicalPath());
		}
	}

	public synchronized void close() throws IOException {
		if(db == null)
			return ;
		db.close();
		db = null;
		if(logger.isDebugEnabled()){
			logger.debug("closed leveldb success");
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * create id
	 * @return
	 */
	public String nextId(){
		return String.valueOf(idWorker.nextId());
	}

	/**
	 * 存储的key使用的是txnId，value为txn的字节流
	 * 
	 * @throws PersistenceException
	 * @return 返回的是txnId
	 */
	public String save(EventSourceBase evt)
			throws PersistenceException {
		EventSourceWrapper wrapper = new EventSourceWrapper(nextId(), evt);
		put(wrapper.getTxnId(), wrapper);
		if(logger.isDebugEnabled()){
			logger.debug(new StringBuilder("saved evt:").append(wrapper.getTxnId()));
		}
		return wrapper.getTxnId();
	}
	
	public void houseKeeping() throws PersistenceException{
		List<EventSourceWrapper> list = null;
		final int count = 500;
		while((list = list(count)).size() > 0){
			String[] ids = toIds(list);
			deleteById(ids);
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("deleted keys:").append(ids));
			}
		}
	}

	/**
	 * @throws PersistenceException
	 *             由于LevelDB不支持分页，所以每次取batchSize容量
	 * @throws
	 */
	public List<EventSourceWrapper> list(int batchSize)
			throws PersistenceException {
		DBIterator iterator = db.iterator();
		iterator.seekToFirst();
		return list(batchSize, iterator);
	}
	
	public <T> T get(String key, Class<T> type) throws PersistenceException{
		try {
			if(null == db)
				return null;
			Serializable value = SerializeUtils.unserialize(db.get(key.getBytes()));
			if(null == value){
				return null;
			}
			return type.cast(value);
		} catch (Exception e) {
			throw new PersistenceException(e);
		} 
	}
	
	public <T> List<T> getMore(Class<T> type, String... keys) throws PersistenceException{
		List<T> list = new ArrayList<T>(keys.length);
		for(String key : keys){
			list.add(get(key, type));
		}
		return list;
	}
	
	public List<EventSourceWrapper> list(int batchSize, DBIterator iterator) throws PersistenceException{
		List<EventSourceWrapper> list = new ArrayList<EventSourceWrapper>();
		try {
			int index = 0;
			for (; index < batchSize && iterator.hasNext(); iterator.next(),index++) {
				Serializable value = SerializeUtils.unserialize(iterator
						.peekNext().getValue());
				list.add((EventSourceWrapper)value);				
			}
			return list;
		} catch (IOException e) {
			throw new PersistenceException(e);
		}
	}
	
	public DBIterator getIterator(){
		return db.iterator();
	}

	public void deleteById(String[] ids) throws PersistenceException {
		WriteBatch wb = db.createWriteBatch();
		try{
			for(String txnId : ids){
				wb.delete(txnId.getBytes());
			}
			db.write(wb);
		}finally{
			try{
				wb.close();
			}catch(Exception e){
				throw new PersistenceException(e);
			}
		}
	}
	
	protected void deleteById(String txnId){
		db.delete(txnId.getBytes());
	}

	protected void put(String key, Serializable ser) throws PersistenceException {
		try {
			db.put(key.getBytes(), SerializeUtils.serialize(ser));
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}
	
	protected void put(String key, Serializable ser, WriteBatch update) throws PersistenceException {
		try {
			update.put(key.getBytes(), SerializeUtils.serialize(ser));
		}catch(Exception e){
			throw new PersistenceException(e);
		}
	}
	
	String[] toIds(List<EventSourceWrapper> list){
		String[] ids = new String[list.size()];
		int index = 0;
		for(EventSourceWrapper evt : list){
			ids[index++] = evt.getTxnId();
		}
		return ids;
	}
	
	public DB getDb() {
		return db;
	}
	
	public synchronized void clear() throws IOException{
		while(_clear() > 0){
			
		}
	}

	protected int _clear() throws IOException{
		DBIterator iterator = db.iterator();
		int index = 0;
		try{
			iterator.seekToFirst();
			if(!iterator.hasNext())
				return 0;
			WriteBatch wb = db.createWriteBatch();
			try{
				for (; index < batchDeleteSize && iterator.hasNext(); iterator.next(),index++) {
					wb.delete(iterator.peekNext().getKey());
				}
				
				db.write(wb);
			}finally{
				wb.close();
			}
		}finally{
			iterator.close();
		}
		return index;
		
	}

	public Boolean getOpenLogger() {
		return openLogger;
	}

	public void setOpenLogger(Boolean openLogger) {
		this.openLogger = openLogger;
	}
}
