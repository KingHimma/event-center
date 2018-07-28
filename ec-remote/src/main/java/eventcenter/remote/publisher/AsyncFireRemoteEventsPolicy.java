package eventcenter.remote.publisher;

import eventcenter.api.ConfigContext;
import eventcenter.api.EventInfo;
import eventcenter.remote.Target;
import eventcenter.remote.utils.StringHelper;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步发送远程事件，线程池将会延迟加载
 * @author JackyLIU
 *
 */
public class AsyncFireRemoteEventsPolicy extends AbstractFireRemoteEventsPolicy {

	public AsyncFireRemoteEventsPolicy(PublishEventCenter eventCenter) {
		super(eventCenter);
	}

	protected ExecutorService threadPool;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void fireRemoteEvents(List<PublisherGroup> groups, final Target target,
			final EventInfo eventInfo, final Object result) {
		ExecutorService tp = getThreadPool(groups.size());
		for(final PublisherGroup group : groups){
			tp.submit(new Runnable(){
				@Override
				public void run() {
					try {
						if(ConfigContext.getConfig().isOpenLoggerMdc()  && StringHelper.isNotEmpty(eventInfo.getMdcValue()))
							MDC.put(ConfigContext.getConfig().getLoggerMdcField(), eventInfo.getMdcValue());
						asyncTransmission(group, target, eventInfo, result);
					}catch(Exception e){
						logger.error(new StringBuilder("fire remote events error, group:").append(group.getGroupName()).append(",remote:").append(group.getRemoteUrl()).append(",event:").append(eventInfo).append(",type:").append(group.getClass()));
					}finally {
						if(ConfigContext.getConfig().isOpenLoggerMdc() && StringHelper.isNotEmpty(eventInfo.getMdcValue()))
							MDC.remove(ConfigContext.getConfig().getLoggerMdcField());
					}
				}
			});
		}
	}
	
	/**
	 * 创建线程池
	 * @param count
	 */
	protected ExecutorService getThreadPool(int count){
		if(null != threadPool)
			return threadPool;
		
		threadPool = Executors.newCachedThreadPool(new ThreadFactory(){
			AtomicInteger tcount = new AtomicInteger(0);
			@Override
			public Thread newThread(Runnable r) {
				String threadName = new StringBuilder("fire_event_").append(tcount.getAndIncrement()).toString();
				if(logger.isTraceEnabled()){
					logger.debug("create thread for async send:" + threadName);
				}
				return new Thread(r, threadName);
			}
		});
		return threadPool;
	}

}
