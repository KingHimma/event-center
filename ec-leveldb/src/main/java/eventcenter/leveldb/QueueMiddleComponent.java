package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventSourceBase;
import eventcenter.api.utils.SerializeUtils;
import eventcenter.leveldb.utils.LevelDbUtils;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * use read cursor and write cursor to store and read data, and create scheduling house-keeping to clear invalidate data
 * @author JackyLIU
 *
 */
public class QueueMiddleComponent {
	
	static final String KEY_READ_CURSOR = "rc";
	
	static final String KEY_WRITE_CURSOR = "wc";

	static final String KEY_DELETE_CURSOR = "dc";

	static final String KEY_PAGE = "p";

	static final String KEY_REAR_PAGE_NO = "rpn";
	
	static final String KEY_HEAD_PAGE_NO = "hpn";
	
	static final String SEPERATOR = "_";
	
	static final int DEFAULT_PAGE_SIZE = 100;
	
	static final Map<String, Boolean> queuesStat = Collections.synchronizedMap(new HashMap<String, Boolean>());

	protected volatile LevelDBCursor readCursor;
	
	protected volatile LevelDBCursor writeCursor;
	
	/**
	 * pop a message wouldn't really delete a data, it just move deleteCursor, data would be deleted by house keeping
	 */
	protected volatile LevelDBCursor deleteCursor;
	
	protected volatile LevelDBPage currentPage;
	
	protected volatile Long rearPageNo;
	
	protected volatile Long headPageNo;
	
	/**
	 * when operating read , it need to lock
	 */
	protected final ReentrantLock readLock = new ReentrantLock();
	
	/**
	 * when operating write, it need to lock
	 */
	protected ReentrantLock writeLock = new ReentrantLock();
	
	/**
	 * when operating delete, it need to lock
	 */
	protected ReentrantLock deleteLock = new ReentrantLock();
	
	/**
	 * maximum size of page
	 */
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	protected final LevelDBPersistenceAdapter adapter;
	
	protected final String queueName;

	/**
	 * 是否开启事务，如果开启，则做houseKeeping的时候，将不会删除event数据，只会删除page数据
	 */
	private boolean openTxn = false;

	/**
	 * 进行compact操作的sst文件大小阈值（100M）
	 */
	public static final long THRESHOLD_SST_FILE_SIZE_OF_COMPACT = 1024 * 1024 * 100;

	/**
	 * create default queue named dq --> defaultQueue
	 * @param adapter
	 */
	public QueueMiddleComponent(LevelDBPersistenceAdapter adapter){
		this(adapter, "dq");
	}
	
	public QueueMiddleComponent(LevelDBPersistenceAdapter adapter, String queueName){
		this.adapter = adapter;
		this.queueName = queueName;
	}

	protected DB getDB(){
		return this.adapter.getDb();
	}

	public String getQueueName() {
		return queueName;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public synchronized void open() throws IOException {
		adapter.open();
		try {
			QueueMiddleComponent.queuesStat.put(queueName, true);
			load();
		} catch (PersistenceException e) {
			if(e.getCause() != null && e.getCause() instanceof IOException){
				throw (IOException)e.getCause();
			}
			logger.error(e.getMessage(), e);
		}
	}
	
	public synchronized void close() throws IOException{
		QueueMiddleComponent.queuesStat.put(queueName, false);
		// scan all queue is closed 
		Collection<Boolean> stats = QueueMiddleComponent.queuesStat.values();
		boolean allClosed = true;
		for(Boolean stat : stats){
			if(stat){
				allClosed = false;
				break;
			}
		}
		
		if(!allClosed) {
			return ;
		}
		adapter.close();
	}
	
	/**
	 * load page info, read,write and delete cursor
	 * @throws PersistenceException 
	 */
	public void load() throws PersistenceException{
		final Lock readLock = this.readLock;
		final Lock writeLock = this.writeLock;
		readLock.lock();
		writeLock.lock();
		try{
			loadPage();
			loadCursor();
		}finally{
			readLock.unlock();
			writeLock.unlock();
		}
	}
	
	String buildKey(String... keys){
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < keys.length;i++){
			if(i > 0){
				sb.append(SEPERATOR);
			}
			sb.append(keys[i]);
		}
		return sb.toString();
	}
	
	protected String wrapperKey(String key){
		return new StringBuilder(queueName).append("_").append(key).toString();
	}

	public boolean isOpenTxn() {
		return openTxn;
	}

	public void setOpenTxn(boolean openTxn) {
		this.openTxn = openTxn;
	}

	public <T> T get(String key, Class<T> type) throws PersistenceException{
		return adapter.get(wrapperKey(key), type);
	}
	
	protected void put(String key, Serializable ser) throws PersistenceException {
		adapter.put(wrapperKey(key), ser);
	}

	protected void put(WriteBatch wb, String key, Serializable ser) throws PersistenceException {
		adapter.put(wrapperKey(key), ser, wb);
	}

	protected void put(String key, Serializable ser, WriteBatch update) throws PersistenceException{
		try{
			update.put(wrapperKey(key).getBytes(), SerializeUtils.serialize(ser));
		}catch(IOException e){
			throw new PersistenceException(e);
		}
	}
	
	protected void loadPage() throws PersistenceException{
		rearPageNo = get(KEY_REAR_PAGE_NO, Long.class);
		if(null == rearPageNo){
			rearPageNo = 0L;
			put(KEY_REAR_PAGE_NO, rearPageNo);			
		}
		headPageNo = get(KEY_HEAD_PAGE_NO, Long.class);
		if(null == headPageNo){
			headPageNo = 0L;
			put(KEY_HEAD_PAGE_NO, headPageNo);			
		}
		
		currentPage = get(buildKey(KEY_PAGE, String.valueOf(rearPageNo)), LevelDBPage.class);
		if(null == currentPage){
			currentPage = new LevelDBPage();
			currentPage.setNo(rearPageNo);
			currentPage.setIndexes(new ArrayList<String>(pageSize));
			put(buildKey(KEY_PAGE, String.valueOf(rearPageNo)), currentPage);
		}
	}
	
	protected void loadCursor() throws PersistenceException{
		readCursor = get(KEY_READ_CURSOR, LevelDBCursor.class);
		if(null == readCursor){
			readCursor = new LevelDBCursor();
			readCursor.setPageNo(headPageNo);
			readCursor.setIndex(0);
			put(KEY_READ_CURSOR, readCursor);
		}
		
		writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
		if(null == writeCursor){
			writeCursor = new LevelDBCursor();
			writeCursor.setPageNo(rearPageNo);
			writeCursor.setIndex(0);
			put(KEY_WRITE_CURSOR, writeCursor);
		}
		
		deleteCursor = get(KEY_DELETE_CURSOR, LevelDBCursor.class);
		if(null == deleteCursor){
			deleteCursor = new LevelDBCursor();
			deleteCursor.setPageNo(headPageNo);
			deleteCursor.setIndex(0);
			put(KEY_DELETE_CURSOR, deleteCursor);
		}
	}
	
	protected Long calculateTotalCount(){
		if(null == writeCursor || readCursor == null) {
			return 0L;
		}
		final long gapPageNum = writeCursor.getPageNo() - readCursor.getPageNo();
		final long leftPageSize = writeCursor.getIndex() - readCursor.getIndex();
		return gapPageNum*pageSize + leftPageSize;
	}
	
	public String save(CommonEventSource evt) throws PersistenceException {
		final ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		WriteBatch wb = adapter.getDb().createWriteBatch();
		EventSourceWrapper wrapper = new EventSourceWrapper(adapter.nextId(), evt);
		try{
			if(adapter.getDb() == null){
				logger.warn("leveldb had been closed, eventId:" + evt.getEventId() + " can't be saved");
				return null;
			}
			if(null == writeCursor){
				writeCursor = get(KEY_WRITE_CURSOR, LevelDBCursor.class);
				if(null == writeCursor){
					writeCursor = new LevelDBCursor();
					writeCursor.setPageNo(rearPageNo);
					writeCursor.setIndex(1);
					put(KEY_WRITE_CURSOR, writeCursor);
				}
			}

			put(wrapper.getTxnId(), wrapper, wb);
			if(isCursorToTheEndPage(writeCursor)){
				newPage(wrapper.getTxnId(), wb);
			}else{
				currentPage.getIndexes().add(wrapper.getTxnId());
				writeCursor.setIndex(currentPage.getIndexes().size());
				put(KEY_WRITE_CURSOR, writeCursor, wb);
				savePage(currentPage, wb);
			}
			DB db = adapter.getDb();
			db.write(wb);
			if(logger.isTraceEnabled()){
				logger.trace(new StringBuilder("save event id:").append(evt.getEventId()).append(" success.").append(", cp:").append(currentPage.getNo()).append(buildCursorLog(", wc:", writeCursor)).append(buildCursorLog(", rc:", readCursor)).append(buildCursorLog(", dc:", deleteCursor)));
			}
			return wrapper.getTxnId();
		}finally{
			try {
				wb.close();
			} catch (IOException e) {
				throw new PersistenceException(e);
			}
			writeLock.unlock();
		}
	}

	private String buildCursorLog(String prefix, LevelDBCursor c){
		return new StringBuilder(prefix).append(c.getPageNo()).append("-").append(c.getIndex()).toString();
	}

	/**
	 * return the head data, and move read cursor ahead. if read cursor equals write cursor, it would return null.
	 * @return
	 * @throws PersistenceException 
	 */
	public EventSourceWrapper pop() throws PersistenceException{
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		if(null == adapter.db) {
			return null;
		}
		WriteBatch wb = adapter.db.createWriteBatch();
		try{
			return pop(wb);
		}finally{
			adapter.db.write(wb);
			try {
				wb.close();
			} catch (Exception e) {
				throw new PersistenceException(e);
			}
			readLock.unlock();
		}
	}

	public EventSourceWrapper pop(WriteBatch wb) throws PersistenceException {
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		try{
			List<EventSourceWrapper> list = pop(1, wb);
			if(list == null || list.size() == 0) {
				return null;
			}
			return list.get(0);
		}finally{
			readLock.unlock();
		}
	}

	public List<EventSourceWrapper> pop(int bulkSize) throws PersistenceException {
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		if(null == adapter.db) {
			return new ArrayList<EventSourceWrapper>();
		}
		WriteBatch wb = adapter.db.createWriteBatch();
		try{
			return pop(bulkSize, wb);
		}finally{
			adapter.db.write(wb);
			try {
				wb.close();
			} catch (Exception e) {
				throw new PersistenceException(e);
			}
			readLock.unlock();
		}
	}

	/**
	 * return a batch head data, and move read cursor ahead. if read cursor equals write cursor, it would return min size data.
	 * @param bulkSize
	 * @return
	 * @throws PersistenceException 
	 */
	public List<EventSourceWrapper> pop(int bulkSize, WriteBatch wb) throws PersistenceException{
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		try{
			long index = readCursor.getIndex();
			List<EventSourceWrapper> list = _peek(bulkSize);
			if(list == null || list.size() == 0) {
				return list;
			}
			long nextIndex = list.size() + index;
			long nextPageNo = calculateNextReadPage(list.size(), nextIndex);
			if(nextPageNo != readCursor.getPageNo()){
				readCursor.setIndex(nextIndex%pageSize);
			}else{
				readCursor.setIndex(nextIndex);
			}
			readCursor.setPageNo(nextPageNo);
			put(wb, KEY_READ_CURSOR, readCursor);
			if(logger.isTraceEnabled()){
				logger.trace(new StringBuilder("pop leveldb :").append(logEventSourceBase(list)).append(", success").append(", cp:").append(currentPage.getNo()).append(buildCursorLog(", wc:", writeCursor)).append(buildCursorLog(", rc:", readCursor)).append(buildCursorLog(", dc:", deleteCursor)));
			}
			return list;
		}finally{
			readLock.unlock();
		}
	}

	String logEventSourceBase(List<EventSourceWrapper> sources){
		if(null == sources || sources.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for(CommonEventSource source : sources){
			if(index > 0){
				sb.append(",");
			}
			if(null == source){
				sb.append("source is null");
			}else {
				sb.append(source.getEventId()).append(" ").append(source.getEventName());
			}
			index++;
		}
		return sb.toString();
	}

	long calculateNextReadPage(int popSize, long nextIndex) throws PersistenceException{
		long increametal = nextIndex/pageSize;
		if(readCursor.getPageNo() == Long.MAX_VALUE && increametal > 0){
			return increametal - 1;
		}
		return increametal + readCursor.getPageNo();
	}
	
	/**
	 * return the head data, but not move read cursor
	 * @return
	 * @throws PersistenceException
	 */
	public EventSourceBase peek() throws PersistenceException{
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		try{
			List<EventSourceWrapper> list = _peek(1);
			if(list == null || list.size() == 0) {
				return null;
			}
			return list.get(0);
		}finally{
			readLock.unlock();
		}
	}
	
	protected List<EventSourceWrapper> _peek(int bulkSize) throws PersistenceException{
		if (writeCursor.getPageNo() < readCursor.getPageNo() || (writeCursor.getPageNo() == readCursor.getPageNo() && writeCursor.getIndex() <= readCursor.getIndex())) {
			if (logger.isTraceEnabled()) {
				logger.trace(new StringBuilder("peek empty").append(", cp:").append(currentPage.getNo()).append(buildCursorLog(", wc:", writeCursor)).append(buildCursorLog(", rc:", readCursor)).append(buildCursorLog(", dc:", deleteCursor)));
			}
			return null;
		}
		
		long startIndex = readCursor.getIndex();
		List<String> txnIds = new ArrayList<String>();
		loadTxnIds(txnIds, (int)startIndex, readCursor.getPageNo(), bulkSize);
		if(txnIds.size() == 0 && logger.isTraceEnabled()) {
			logger.trace(new StringBuilder("_peek txnIds size is zero!").append("cp:").append(currentPage.getNo()).append(buildCursorLog(",wc:", writeCursor)).append(buildCursorLog(", rc:", readCursor)).append(buildCursorLog(", dc:", deleteCursor)));
		}
		List<EventSourceWrapper> list = new ArrayList<EventSourceWrapper>(txnIds.size());
		for(String txnId : txnIds){
			EventSourceWrapper wrapper = get(txnId, EventSourceWrapper.class);
			list.add(wrapper);	// TODO 这段代码有问题? <-----
		}
		return list;
	}
	
	public List<EventSourceWrapper> peek(int bulkSize) throws PersistenceException{
		final ReentrantLock readLock = this.readLock;
		readLock.lock();
		try{
			return _peek(bulkSize);
		}finally{
			readLock.unlock();
		}
	}
	
	void loadTxnIds(final List<String> txnIds, int startIndex, long pageNo, int total) throws PersistenceException{
		LevelDBPage page = getPage(pageNo);
		if(null == page || page.getIndexes() == null || page.getIndexes().size() == 0) {
			return ;
		}
		for(int i = (int)startIndex;i < page.getIndexes().size();i++){
			txnIds.add(page.getIndexes().get(i));
			if(txnIds.size() == total) {
				return ;
			}
		}
		if(page.getNo() == currentPage.getNo()) {
			return ;
		}
		long nextPageNo = page.getNo()==Long.MAX_VALUE?0:(page.getNo() + 1);
		loadTxnIds(txnIds, 0, nextPageNo, total);
	}
	
	boolean isCursorToTheEndPage(LevelDBCursor c){
		long index = c.getIndex();
		return index >= pageSize;
	}
	
	void newPage(String txnId, WriteBatch wb) throws PersistenceException{
		// TODO once page number is to the LONG maximum, it need to move page whole
		LevelDBPage page = new LevelDBPage();
		if(currentPage.getNo() == Long.MAX_VALUE){
			currentPage.setNo(0);
		}else{
			page.setNo(currentPage.getNo() + 1);
		}
		page.setIndexes(new ArrayList<String>());
		if(null != txnId && !"".equals(txnId)){
			page.getIndexes().add(txnId);
		}
		saveRearPageNo(page.getNo(), wb);
		
		currentPage = page;
		savePage(currentPage, wb);
		
		writeCursor.setPageNo(page.getNo());
		writeCursor.setIndex(1);
		put(KEY_WRITE_CURSOR, writeCursor, wb);
	}
	
	void saveRearPageNo(Long pageNo, WriteBatch wb) throws PersistenceException{
		rearPageNo = pageNo;
		put(KEY_REAR_PAGE_NO, rearPageNo, wb);
	}
	
	void savePage(LevelDBPage page, WriteBatch wb) throws PersistenceException{
		put(buildKey(KEY_PAGE, String.valueOf(page.getNo())), page, wb);
	}
	
	LevelDBPage getPage(long pageNo) throws PersistenceException{
		return get(buildKey(KEY_PAGE, String.valueOf(pageNo)), LevelDBPage.class);
	}
	
	public long count(){
		return calculateTotalCount();
	}
	
	/**
	 * 获取未被删除，但是已经pop的消息数量
	 * @return
	 */
	public long popCount(){
		final long gapPageNum = readCursor.getPageNo() - deleteCursor.getPageNo();
		final long leftPageSize = readCursor.getIndex() - deleteCursor.getIndex();
		return gapPageNum*pageSize + leftPageSize;
	}
	
	public void houseKeeping() throws PersistenceException {
		final ReentrantLock deleteLock = this.deleteLock;
		final ReentrantLock readLock = this.readLock;
		if(deleteLock.tryLock()){
			try{
				readLock.lock();
				final long pageNo = headPageNo;
				long readPageNo = readCursor.getPageNo();
				long readIndex = readCursor.getIndex();
				try {
					if (deleteCursor.getPageNo() == headPageNo &&
							readCursor.getPageNo() == deleteCursor.getPageNo() && deleteCursor.getIndex() == readCursor.getIndex()) {
						return;
					}
				}finally{
					readLock.unlock();
				}

				for(int i = (int)pageNo;i < readPageNo;i++){
					LevelDBPage page = getPage(i);
					List<String> indexes = page.getIndexes();
					if(indexes == null || indexes.size() == 0) {
						continue;
					}
					batchDelete(i, indexes.size(), indexes.toArray(new String[indexes.size()]));
					if(logger.isTraceEnabled()) {
						logger.trace(String.format("delete data page:%s, from:%s, to:%s", readPageNo, 0, readIndex));
					}
				}
				if(readIndex > 0){
					LevelDBPage page = getPage(readPageNo);
					int startIndex = 0;
					if(deleteCursor.getPageNo() == readPageNo){
						startIndex = (int)deleteCursor.getIndex();
					}
					List<String> deleteIndexes = page.getIndexes().subList(startIndex, (int)readIndex);
					if(deleteIndexes.size() > 0){
						batchDelete(readPageNo, (int)readIndex, deleteIndexes.toArray(new String[deleteIndexes.size()]));
						if(logger.isTraceEnabled()) {
							logger.trace(String.format("delete data page:%s, from:%s, to:%s", readPageNo, startIndex, readIndex));
						}
					}
				}

				//TODO 为何不将删除页操作连同删除索引一起进行，这里应该可以优化
				// delete page index
				if(pageNo < deleteCursor.getPageNo()){
					WriteBatch writeBatch = adapter.getDb().createWriteBatch();
					try{
						for(int i = (int)pageNo;i < deleteCursor.getPageNo();i++){
							String key = wrapperKey(buildKey(KEY_PAGE, String.valueOf(i)));
							writeBatch.delete(key.getBytes());
							if(logger.isTraceEnabled()) {
								logger.trace("delete page:" + key);
							}
						}
						adapter.getDb().write(writeBatch);
					}catch(Exception e){
						throw new PersistenceException(e);
					}finally{
						try {
							writeBatch.close();
						} catch (IOException e) {
							throw new PersistenceException(e);
						}
					}
				}

				// 由于leveldb的删除机制，要删除sst文件需要进行compact操作，但是如果每次houseKeeping时均进行compact可能会对
				// 性能造成一定的影响，故需要考虑compact操作的时机，这里采用的策略为在sst文件大小超过阈值时进行一次compact操作
				LevelDbUtils.SstFileStatisticsInfo sstFileStatisticsInfo = LevelDbUtils.generateSstFileStatisticsInfo(adapter);
				if (sstFileStatisticsInfo.getFileSize() > THRESHOLD_SST_FILE_SIZE_OF_COMPACT) {
					adapter.getDb().compactRange(null, null);
				}
			}finally{
				deleteLock.unlock();
			}
		}
	}
	
	protected void batchDelete(long pageNo, int index, String... keys) throws PersistenceException{
		if(keys == null || keys.length == 0 || adapter.getDb() == null) {
			return ;
		}
		WriteBatch writeBatch = adapter.getDb().createWriteBatch();
		try{
			// 未开启事务时，才能删除存储的事件数据，如果开启事务后，将由TxnQueueComponent中的houseKeeping处理
			if(!isOpenTxn()) {
				for (String key : keys) {
					writeBatch.delete(wrapperKey(key).getBytes());
				}
			}
			
			deleteCursor.setPageNo(pageNo);
			deleteCursor.setIndex(index);
			put(KEY_DELETE_CURSOR, deleteCursor, writeBatch);
			headPageNo = pageNo;
			put(buildKey(KEY_HEAD_PAGE_NO), headPageNo, writeBatch);
			adapter.getDb().write(writeBatch);
		}catch(Exception e){
			throw new PersistenceException(e);
		}finally{
			try {
				writeBatch.close();
			} catch (IOException e) {
				throw new PersistenceException(e);
			}
		}
	}
	
	public synchronized void clear() throws Exception {
		final Lock readLock = this.readLock;
		final Lock writeLock = this.writeLock;
		readLock.lock();
		writeLock.lock();
		try{
			adapter.clear();
			load();
		}finally{
			readLock.unlock();
			writeLock.unlock();
		}
	}
}
