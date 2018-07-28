package eventcenter.api.async.simple;

import java.util.concurrent.ThreadPoolExecutor;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.QueueEventContainerFactory;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.async.QueueEventContainerFactory;

/**
 * 简单异步容器的工厂
 * @author JackyLIU
 *
 */
public class SimpleQueueEventContainerFactory implements
        QueueEventContainerFactory {

	private Integer queueCapacity;
	
	/**
	 * 线程池的核心线程数
	 */
	private Integer corePoolSize;
	
	/**
	 * 线程池的最大线程数
	 */
	private Integer maximumPoolSize;
	
	private ThreadPoolExecutor threadPool;
	
	public Integer getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(Integer queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	@Override
	public QueueEventContainer createContainer(EventCenterConfig config) {
		if(null != threadPool)		
			return new SimpleQueueEventContainer(threadPool, config, createDefaultEventQueue());
		if(null == corePoolSize || null == maximumPoolSize)
			return new SimpleQueueEventContainer(config, createDefaultEventQueue());
		
		return new SimpleQueueEventContainer(corePoolSize, maximumPoolSize, config, createDefaultEventQueue());
	}
	
	private EventQueue createDefaultEventQueue(){
		if(null == queueCapacity)
			return new SimpleEventQueue();
		return new SimpleEventQueue(queueCapacity);
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

}
