package eventcenter.leveldb;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventListener;
import eventcenter.api.EventListenerTask;
import eventcenter.api.EventSourceBase;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.tx.EventTxnStatus;
import eventcenter.api.tx.ResumeTxnHandler;
import eventcenter.leveldb.tx.TransactionConfig;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LevelDBContainer extends QueueEventContainer {

	protected ThreadPoolExecutor threadPool;
	
	protected ListenQueueThread listenQueueThread;
	
	protected int corePoolSize = Runtime.getRuntime().availableProcessors();
	
	protected int maximumPoolSize = corePoolSize*2;

	// 60 seconds
	protected int keepAliveTime = 60;
	
	protected final Object locker = new Object();
	
	protected ArrayBlockingQueue<Runnable> bockingQueue;
	
	/**
	 * it would calculate blocking queue capacity by maximumPoolSize * factory
	 */
	protected float blockingQueueFactor = 0.1f;
	
	protected int blockingQueueSize;

	/**
	 * 遍历循环队列中的元素间隔时间，单位毫秒
	 */
	protected long loopQueueInterval = 1000;

	/**
	 * 容器可事务化配置，如果需要容器支持
	 */
	protected TransactionConfig transactionConfig;

	protected final AtomicLong counter = new AtomicLong(0);
	
	public LevelDBContainer(EventCenterConfig config,
			EventQueue queue) {
		super(config, queue);
	}

	protected ThreadPoolExecutor createThreadPool(){
		bockingQueue = new ArrayBlockingQueue<Runnable>(blockingQueueSize);
		return new InnerThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, bockingQueue, locker);
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public float getBlockingQueueFactor() {
		return blockingQueueFactor;
	}

	public void setBlockingQueueFactor(float blockingQueueFactor) {
		this.blockingQueueFactor = blockingQueueFactor;
	}

	public long getLoopQueueInterval() {
		return loopQueueInterval;
	}

	public void setLoopQueueInterval(long loopQueueInterval) {
		this.loopQueueInterval = loopQueueInterval;
	}

	/**
	 * 容器可事务化配置，如果需要容器支持
	 * @return
	 */
	public TransactionConfig getTransactionConfig() {
		return transactionConfig;
	}

	/**
	 * 容器可事务化配置，如果需要容器支持
	 * @param transactionConfig
	 */
	public void setTransactionConfig(TransactionConfig transactionConfig) {
		this.transactionConfig = transactionConfig;
	}

	@Override
	public void startup() throws Exception {
		this.blockingQueueSize = (int)(maximumPoolSize * blockingQueueFactor);
		if(this.blockingQueueSize < this.corePoolSize){
			this.blockingQueueSize = this.corePoolSize;
		}
		this.threadPool = createThreadPool();
		this.listenQueueThread = new ListenQueueThread(locker, this);
		LevelDBQueue _q = (LevelDBQueue)queue;
		if(this.transactionConfig != null)
			_q.setTxnCapacity(maximumPoolSize + this.blockingQueueSize + 20);	// it must be set
		_q.open();
		this.listenQueueThread.start();
		logger.info("leveldb container startup success");
	}

	@Override
	public void shutdown() throws Exception {
		this.listenQueueThread.close();
		this.threadPool.shutdownNow();
		getLevelDBQueue().close();

		synchronized(this.locker){
			this.locker.notifyAll();
		}
		logger.info("leveldb container closed success");
	}

	/**
	 * 这个容器会将事件缓存在leveldb文件数据库中，所以他是支持持久化的容器
	 * @return
	 */
	@Override
	public boolean isPersisted() {
		return true;
	}

	@Override
	public boolean isIdle() {
		//return bockingQueue.size() < maximumPoolSize;
		return counter.get() < maximumPoolSize;
	}

	public void commitTransaction(EventTxnStatus txn){
		try {
			if(!listenQueueThread.flag){
				// TODO 这段代码需要慎重对待
				return ;
			}
			getLevelDBQueue().commit(txn);
		} catch (Exception e) {
			logger.error("commit txn[" + txn.getEventId() + "] error:" + e.getMessage(), e);
		}
	}
	
	class InnerThreadPool extends ThreadPoolExecutor {

		private final Object locker;
		
		public InnerThreadPool(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, Object locker) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
			this.locker = locker;
		}
		
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			synchronized(this.locker){
				this.locker.notifyAll();
			}
			counter.decrementAndGet();
		}
	}

	private LevelDBQueue getLevelDBQueue(){
		return (LevelDBQueue)queue;
	}

	/**
	 * 检查是否存在未提交的事件，当线程启动时开启检查
	 * @return
	 */
	boolean checkUncommitTransactions(){
		if(null == transactionConfig)
			return false;

		try {
			int count = getLevelDBQueue().getTxnQueueComponent().countOfTxn();
			if(count > 0){
				if(logger.isDebugEnabled()){
					logger.debug("found " + count + " txn not committed.it would resume txn thread");
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("check txn queue component's count error:" + e.getMessage());
			return false;
		}
	}

	protected EventListener findAsyncEventListeners(EventSourceBase message, Class<? extends EventListener> type){
		List<EventListener> listeners = super.findAsyncEventListeners(message);
		if(listeners.size() == 0){
			return null;
		}
		for(EventListener listener : listeners){
			if(listener.getClass() == type)
				return listener;
		}
		return null;
	}
	
	/**
	 * it listen event queue, the first it would check thread pool whether it is idle state. if thread pool is idle, it would 
	 * transfer from queue, if busy, it would wait for thread complete in pool, once a thread complete a task, it would active 
	 * thread and continue to transfer from queue.
	 * @author JackyLIU
	 *
	 */
	class ListenQueueThread extends Thread{
		
		private final Object locker;
		
		private volatile boolean flag = true;

		private final LevelDBContainer containerRef;
		
		public ListenQueueThread(Object locker, LevelDBContainer containerRef){
			super("listen-queue-thread");
			this.locker = locker;
			this.containerRef = containerRef;
		}
		
		@Override
		public void run() {
			if(checkUncommitTransactions()){
				// 消费缓存队列事件之前，先检查事务队列中是否还存在未提交的事务，如果存在，则需要重新执行
				try{
					getLevelDBQueue().getTxnQueueComponent().resumeTxn(new ResumeTxnHandler() {
						@Override
						public void resume(EventTxnStatus status, EventSourceBase event) {
							EventListener listener = findAsyncEventListeners(event, status.getListenerType());
							if(null == listener){
								logger.warn(new StringBuilder("txnId[").append(status.getTxnId()).append("], evt:").append(event).append(" can't found listener:").append(status.getListenerType()).append(", resume failure"));
								return ;
							}
							consumeEvent(event, listener, status);
							if(logger.isDebugEnabled()){
								logger.debug(String.format("resumed %s event success.", event.toString()));
							}
						}
					});
					if(logger.isDebugEnabled()){
						logger.debug("resumed uncommitted txn success!");
					}
				}catch(Throwable e){
					logger.fatal("resume uncommitted transaction failure:" + e.getMessage(), e);
				}
			}

			while(flag){
				try {
					if(null == transactionConfig) {
						listenQueue();
					}else{
						listenQueueWithTxn();
					}
				}catch(Throwable e){
					logger.fatal("happend fatal error on queue.transfer:" + e.getMessage());
				}
			}
		}

		private void listenQueue(){
			long start = System.currentTimeMillis();
			EventSourceBase evt = queue.transfer(loopQueueInterval);
			if (null == evt) {
				return ;
			}
			if (logger.isTraceEnabled()) {
				logger.trace(new StringBuilder("transfer evt:").append(evt).append(" success. wait:").append(System.currentTimeMillis() - start).append(" ms."));
			}

			List<EventListener> listeners = findAsyncEventListeners(evt);
			if (null == listeners || listeners.size() == 0)
				return ;

			consumeEvent(evt, listeners, null);
		}

		private void listenQueueWithTxn(){
			long start = System.currentTimeMillis();
			DB db = getLevelDBQueue().getQueueMiddleComponent().getDB();
			WriteBatch wb = db.createWriteBatch();
			EventSourceBase evt = null;
			List<EventListener> listeners = null;
			List<EventTxnStatus> txnList = null;
			try {
				evt = getLevelDBQueue().transfer(loopQueueInterval, wb);
				if (null == evt) {
					return;
				}
				if (logger.isTraceEnabled()) {
					logger.trace(new StringBuilder("transfer evt:").append(evt).append(" success. wait:").append(System.currentTimeMillis() - start).append(" ms."));
				}

				listeners = findAsyncEventListeners(evt);
				if (null == listeners || listeners.size() == 0)
					return;
				txnList = beginTransaction(evt, listeners, wb);
			}finally{
				db.write(wb);
				try {
					wb.close();
				} catch (Throwable e) {
					logger.error("consume event with txn failure:" + e.getMessage(), e);
				}
			}
			if(listeners != null && listeners.size() > 0) {
				EventSourceWrapper wrapper = (EventSourceWrapper)evt;
				consumeEvent(wrapper.getEvt(), listeners, txnList);
			}
		}

		private void consumeEvent(final EventSourceBase evt, final List<EventListener> listeners, final List<EventTxnStatus> txns){
			int index = 0;
			for (EventListener listener : listeners) {
				EventTxnStatus txn = null;
				if(null != txns && txns.size() > 0) {
					try {
						txn = txns.get(index++);
					}catch(Exception e){
						logger.error("get txn error:" + e.getMessage(), e);
					}
				}
				consumeEvent(evt, listener, txn);
			}
		}

		private void consumeEvent(EventSourceBase evt, final EventListener listener, final EventTxnStatus txn){
			boolean innerFlag = true;
			EventListenerTask task = null;
			if(evt instanceof EventSourceWrapper){
				evt = ((EventSourceWrapper)evt).getEvt();
			}
			if(txn == null) {
				task = new EventListenerTask(listener, evt);
			}else{
				task = new TxEventListenerTask(listener, evt, this.containerRef, txn);
			}
			while (innerFlag && flag) {
				try {
					threadPool.execute(task);
					innerFlag = false;
					counter.incrementAndGet();
				} catch (RejectedExecutionException e) {
					if(logger.isTraceEnabled()){
						logger.trace(e.getMessage(), e);
					}
					waitForRelease(loopQueueInterval);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					innerFlag = false;
					if(null != txn){
						commitTransaction(txn);
					}
				}
			}
		}

		private List<EventTxnStatus> beginTransaction(final EventSourceBase evt, final List<EventListener> listeners, final WriteBatch writeBatch){
			EventSourceWrapper wrapper = (EventSourceWrapper)evt;
			List<EventTxnStatus> txnList = new ArrayList<EventTxnStatus>(listeners.size() + 1);
			for(EventListener listener : listeners){
				try {
					txnList.add(getLevelDBQueue().getTxnStatus(wrapper.getEvt(), wrapper.getTxnId(), listener, writeBatch));
				} catch (Exception e) {
					logger.error("getTxnStatus error:" + e.getMessage(), e);
				}
			}
			return txnList;
		}
		
		/*private void waitForRelease(){
			waitForRelease(-1L);
		}*/
		
		private void waitForRelease(long timeout){
			final Object locker = this.locker;
			synchronized(locker){
				try {
					if(timeout == -1L)
						locker.wait();
					else
						locker.wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void close(){
			flag = false;
			final Object locker = this.locker;
			synchronized(locker){
				locker.notifyAll();
			}
		}
	}

	@Override
	public int countOfMaxConcurrent() {
		if(null == threadPool)
			return 0;
		return threadPool.getMaximumPoolSize();
	}

	@Override
	public int countOfLiveThread() {
		return counter.intValue();
	}

	@Override
	public int countOfQueueBuffer() {
		if(null == threadPool)
			return 0;
		return threadPool.getQueue().size();
	}
}
