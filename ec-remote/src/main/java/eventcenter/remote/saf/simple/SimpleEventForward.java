package eventcenter.remote.saf.simple;

import eventcenter.api.async.EventQueue;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.saf.AbstractEventForward;
import eventcenter.remote.publisher.PublisherGroup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleEventForward extends AbstractEventForward {

	protected Map<PublisherGroup, EventQueue> monitors;
	
	protected Map<PublisherGroup, WatchDog> watchDogs;
	
	protected ExecutorService executorService;

	public SimpleEventForward(){}
	
	public SimpleEventForward(boolean storeOnSendFail) {
		super(storeOnSendFail);
	}

	@Override
	public void startup(Map<PublisherGroup, EventQueue> monitors) {
		if(null == this.monitors)
			this.monitors = new HashMap<PublisherGroup, EventQueue>();
		if(null != monitors) {
			this.monitors.putAll(monitors);
		}
		this.watchDogs = new HashMap<PublisherGroup, WatchDog>();
		final AtomicInteger count = new AtomicInteger(1);
		executorService = Executors.newCachedThreadPool(new ThreadFactory(){
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, new StringBuilder("ec-saf-thread-").append(count.getAndIncrement()).toString());
			}
		});

		startup = true;
		Set<PublisherGroup> ets = monitors.keySet();
		for(PublisherGroup group : ets){
			watchDogs.put(group, new WatchDog(group, monitors.get(group)));
			executorService.execute(watchDogs.get(group));
		}
		logger.info(new StringBuilder("startup SimpleEventForward success, size of monitors:").append(monitors.size()));
	}

	@Override
	public void shutdown() {
		startup = false;
		Collection<WatchDog> watchDogs = this.watchDogs.values();
		for(WatchDog watchDog : watchDogs){
			try {
				watchDog.shutdown();
			}catch(Throwable e){
				logger.error(e.getMessage(), e);
			}
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info(new StringBuilder("shutdown SimpleEventForward success"));
	}

	public Map<PublisherGroup, EventQueue> getMonitors() {
		return monitors;
	}

	@Override
	public boolean addMonitor(PublisherGroup publisherGroup,
			EventQueue eventQueue) {
		if(null == monitors)
			monitors = new HashMap<PublisherGroup, EventQueue>();
		if(monitors.containsKey(publisherGroup))
			return false;
		
		monitors.put(publisherGroup, eventQueue);
		watchDogs.put(publisherGroup, new WatchDog(publisherGroup, monitors.get(publisherGroup)));
		executorService.execute(watchDogs.get(publisherGroup));
		return true;
	}

	@Override
	public boolean removeMonitor(PublisherGroup publisherGroup) throws IOException {
		if(!monitors.containsKey(publisherGroup))
			return false;
		
		watchDogs.get(publisherGroup).shutdown();
		watchDogs.remove(publisherGroup);
		
		EventQueue eventQueue = monitors.remove(publisherGroup);
		if(null != eventQueue)
			eventQueue.close();
		return true;
	}

}
