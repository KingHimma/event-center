package eventcenter.api.async.simple;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventListener;
import eventcenter.api.EventListenerTask;
import eventcenter.api.EventSourceBase;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.MessageListener;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.EventListener;
import eventcenter.api.EventListenerTask;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.MessageListener;
import eventcenter.api.async.QueueEventContainer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 此版本使用了{@link SimpleEventQueue}作为队列
 * @author JackyLIU
 *
 */
public class SimpleQueueEventContainer extends QueueEventContainer {
	
	/**
	 * 线程池
	 */
	private ThreadPoolExecutor threadPool;
	
	/**
	 * 默认读取队列元素的超时时间
	 */
	private long transferTimeout = 1000;

	protected final AtomicLong counter = new AtomicLong(0);
	
	public SimpleQueueEventContainer(EventCenterConfig config, EventQueue queue){
		super(config, queue);
		this.threadPool = new InnerThreadPool(0, 100, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}
	
	public SimpleQueueEventContainer(int corePoolSize, int maximumPoolSize, EventCenterConfig config, EventQueue queue){
		super(config, queue);
		this.threadPool = new InnerThreadPool(corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES, new SynchronousQueue<Runnable>());
	}
	
	public SimpleQueueEventContainer(ThreadPoolExecutor threadPool, EventCenterConfig config, EventQueue queue){
		super(config, queue);
		this.threadPool = threadPool;
	}
	
	public long getTransferTimeout() {
		return transferTimeout;
	}

	public void setTransferTimeout(long transferTimeout) {
		this.transferTimeout = transferTimeout;
	}

	ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}
	
	public void startup() throws Exception{
		queue.setMessageListener(new MessageListener(){
			@Override
			public void onMessage(final EventSourceBase message) {
				List<EventListener> listeners = findAsyncEventListeners(message);
				if(null == listeners || listeners.size() == 0)
					return ;

				for(EventListener listener : listeners){
					threadPool.submit(new EventListenerTask(listener, message));
					counter.incrementAndGet();
				}
			}
		});
		if(logger.isDebugEnabled()){
			logger.debug(new StringBuilder("start SimpleQueueEventContainer success"));
		}
	}
	
	public void shutdown() throws Exception{
		queue.close();
		threadPool.shutdownNow();
	}

	@Override
	public boolean isIdle() {
		return threadPool.getActiveCount() == 0;
	}

	/**
	 * 简单版的不支持持久化
	 * @return
	 */
	@Override
	public boolean isPersisted() {
		return false;
	}

	@Override
	public int countOfMaxConcurrent() {
		return threadPool.getMaximumPoolSize();
	}

	@Override
	public int countOfLiveThread() {
		return counter.intValue();
	}

	@Override
	public int countOfQueueBuffer() {
		return threadPool.getQueue().size();
	}

	class InnerThreadPool extends ThreadPoolExecutor {

		public InnerThreadPool(int corePoolSize, int maximumPoolSize,
							   long keepAliveTime, TimeUnit unit,
							   BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			counter.decrementAndGet();
		}
	}
}
