package eventcenter.api.aggregator.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.*;
import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单高效的聚合运行容器，使用了JDK的线程池消费事件
 * @author JackyLIU
 *
 */
public class SimpleAggregatorContainer implements AggregatorContainer {

	/**
	 * 线程池
	 */
	protected final ThreadPoolExecutor threadPool;

	protected final Logger logger = Logger.getLogger(this.getClass());

	public SimpleAggregatorContainer(int corePoolSize, int maximumPoolSize){
		this(createDefaultThreadPool(corePoolSize, maximumPoolSize));
	}
	
	public SimpleAggregatorContainer(ThreadPoolExecutor threadPool){
		this.threadPool = threadPool;
		if(this.threadPool.getThreadFactory() == null) {
			this.threadPool.setThreadFactory(new DefaultThreadFactory());
		}
	}
	
	public SimpleAggregatorContainer(){
		this.threadPool = createDefaultThreadPool(0,100);
	}

	protected static ThreadPoolExecutor createDefaultThreadPool(int corePoolSize, int maximumPoolSize){
		return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new DefaultThreadFactory());
	}
	
	ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	@PreDestroy
	public void close(){
		this.threadPool.shutdownNow();
	}

	@Override
	public ListenersConsumedResult executeListeners(
			List<AggregatorEventListener> listeners, CommonEventSource source, ListenerExceptionHandler handler) throws InterruptedException {
		return executeListeners(listeners, source, handler, threadPool);
	}

	protected ListenersConsumedResult executeListeners(List<AggregatorEventListener> listeners, CommonEventSource source, ListenerExceptionHandler handler, ThreadPoolExecutor executor) throws InterruptedException {
		long start = System.currentTimeMillis();
		List<Future<ListenerConsumedResult>> tasks = executor.invokeAll(createListenerCallers(listeners,source,handler));

		ListenersConsumedResult list = new ListenersConsumedResult();
		for(Future<ListenerConsumedResult> task : tasks){
			try {
				list.getResults().add(task.get());
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
			}
		}
		list.setEventName(source.getEventName());
		list.setSource(source);
		list.setTook(System.currentTimeMillis() - start);
		return list;
	}

	protected List<ListenerCaller> createListenerCallers(List<AggregatorEventListener> listeners, CommonEventSource source, ListenerExceptionHandler handler){
		List<ListenerCaller> list = new ArrayList<ListenerCaller>(listeners.size());
		for(AggregatorEventListener listener : listeners){
			list.add(new ListenerCaller(listener, (AggregatorEventSource)source, handler));
		}
		return list;
	}

	protected List<ListenerCaller> createListenerCallers(AggregatorEventListener listener, List<CommonEventSource> sources, ListenerExceptionHandler handler){
		List<ListenerCaller> list = new ArrayList<ListenerCaller>(sources.size());
		for(CommonEventSource source : sources){
			list.add(new ListenerCaller(listener, (AggregatorEventSource)source, handler));
		}
		return list;
	}
	
	protected class ListenerCaller implements Callable<ListenerConsumedResult>{

		private final AggregatorEventListener eventListener;
		
		private final AggregatorEventSource source;
		
		private final ListenerExceptionHandler handler;
		
		public ListenerCaller(AggregatorEventListener eventListener, AggregatorEventSource source, ListenerExceptionHandler handler){
			this.eventListener = eventListener;
			this.source = source;
			this.handler = handler;
		}
		
		@Override
		public ListenerConsumedResult call() throws Exception {
			ListenerConsumedResult result = new ListenerConsumedResult();
			long start = System.currentTimeMillis();
			try{
				eventListener.onObserved(source);
				result.setResult(source.getResult(eventListener));
			}catch(Exception e){
				logger.error(e.getMessage(), e);
				result.setResult(handler.handle(eventListener, source, e));
				result.setError(true);
			}
			long took = System.currentTimeMillis() - start;
			result.setTook(took);
			result.setListenerType(eventListener.getClass());
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("aggregator event complete:").append(source.getEventName()).append(", took:").append(took).append(", listener:").append(result.getListenerType()));
			}
			return result;
		}
		
	}

	protected ListenersConsumedResult executeListenerSources(AggregatorEventListener listener, List<CommonEventSource> sources,
															 ListenerExceptionHandler handler, ThreadPoolExecutor executor) throws InterruptedException {
		long start = System.currentTimeMillis();
		List<Future<ListenerConsumedResult>> tasks = executor.invokeAll(createListenerCallers(listener,sources,handler));

		ListenersConsumedResult list = new ListenersConsumedResult();
		for(Future<ListenerConsumedResult> task : tasks){
			try {
				list.getResults().add(task.get());
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
			}
		}
		list.setTook(System.currentTimeMillis() - start);
		return list;
	}

	@Override
	public ListenersConsumedResult executeListenerSources(
            AggregatorEventListener listener, List<CommonEventSource> sources,
            ListenerExceptionHandler handler) throws InterruptedException {
		return executeListenerSources(listener, sources, handler, threadPool);
	}

	/**
	 * The default thread factory
	 */
	protected static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			namePrefix = "ec-aggr-pool-" +
					poolNumber.getAndIncrement() +
					"-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

}
