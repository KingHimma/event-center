package eventcenter.remote.saf;

import eventcenter.api.CommonEventSource;
import eventcenter.api.async.EventQueue;
import eventcenter.remote.EventInfoSource;
import eventcenter.remote.Target;
import eventcenter.remote.publisher.AbstractFireRemoteEventsPolicy;
import eventcenter.remote.publisher.PublisherGroup;
import org.apache.log4j.Logger;

/**
 * 简单高效的事件推送服务
 * @author JackyLIU
 *
 */
public abstract class AbstractEventForward implements EventForward {
	
	protected volatile boolean startup = false;
	
	/**
	 * 监控间隔，默认为60000毫秒，也就是一分钟一次
	 */
	protected long checkInterval = 60000;
	
	protected boolean storeOnSendFail;

	protected AbstractFireRemoteEventsPolicy policy;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
			
	public AbstractEventForward(){
		
	}
	
	/**
	 * 
	 * @param storeOnSendFail 是否为发送失败时才存储事件
	 */
	public AbstractEventForward(boolean storeOnSendFail){
		this.storeOnSendFail = storeOnSendFail;
	}
	
	public void setStoreOnSendFail(boolean storeOnSendFail) {
		this.storeOnSendFail = storeOnSendFail;
	}

	public long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	@Override
	public boolean isStartup() {
		return startup;
	}

	@Override
	public void setFireRemoteEventsPolicy(AbstractFireRemoteEventsPolicy policy) {
		this.policy = policy;
	}

	/**
	 * 推送事件
	 * @param publisherGroup
	 * @param eventQueue
	 * @throws TransmissionException 
	 */
	public void forward(PublisherGroup publisherGroup, EventQueue eventQueue) throws TransmissionException{
		CommonEventSource element = null;
		boolean flag = true;
		while(flag){
			element = eventQueue.transfer(100);
			// 如果storeOnSendFail为true，那么当queue为空了，就需要跳出forward方法
			if(null == element && storeOnSendFail){
				return ;
			}else if(null == element){
				continue;
			}
			
			forward(publisherGroup, element);
		}
	}
	
	public void forward(PublisherGroup publisherGroup, CommonEventSource element) throws TransmissionException{
		Object source = element.getSource();
		if(source == null && element instanceof EventInfoSource){
			source = ((EventInfoSource)element).getTarget();
		}
		if(!(source instanceof Target)){
			source = new Target(source.getClass().getName());
		}
		EventInfoSource ces = (EventInfoSource)element;
		try{
			if(null == policy){
				publisherGroup.getEventTransmission().asyncTransmission((Target) source, ces.getEventInfo(), ces.getResult());
				if(logger.isDebugEnabled()){
					logger.debug(new StringBuilder("forward event:").append(ces).append(" success"));
				}
			}else{
				policy.asyncTransmission(publisherGroup, (Target)source, ces.getEventInfo(), ces.getResult());
			}
		}catch(IllegalStateException e){
			// ignore the exception
			logger.error(new StringBuilder("forward failure, it would be ignored forward:").append(e.getMessage()), e);
		}catch(Exception e){
			logger.error(new StringBuilder("forward failure:").append(e.getMessage()), e);
			throw new TransmissionException(e, element);
		}
	}

	public AbstractFireRemoteEventsPolicy getFireRemoteEventsPolicy() {
		return policy;
	}

	/**
	 * 看门狗程序
	 * @author JackyLIU
	 *
	 */
	public class WatchDog implements Runnable{

		protected final PublisherGroup publisherGroup;
		
		protected final EventQueue eventQueue;
		
		/**
		 * 上一次发送失败的元素
		 */
		protected CommonEventSource lastFailEvt;
		
		protected volatile boolean watchOpen = true;

		protected final Object lock = new Object();
		
		public WatchDog(PublisherGroup publisherGroup, EventQueue eventQueue){
			this.publisherGroup = publisherGroup;
			this.eventQueue = eventQueue;
		}
		
		@Override
		public void run() {
			while(watchOpen){
				if(!checkHealth()){

				}else{
					// 先判断之前发送的元素存在，存在表示上一次发送失败了，那么将重试上一次的元素
					if(null != lastFailEvt){
						try {
							forward(publisherGroup, lastFailEvt);
							lastFailEvt = null;
						} catch (TransmissionException e) {
							sleep();
							// TODO 实现监控
							continue;
						}
					}
					
					try{
						forward(publisherGroup, eventQueue);
					}catch(TransmissionException e){
						lastFailEvt = e.getEvt();
						// TODO 实现监控
					}catch(Exception e){
						// TODO 实现监控
						logger.error(new StringBuilder("forward failure:").append(e.getMessage()), e);
					}
				}
				
				sleep();
			}
			if(logger.isDebugEnabled()){
				logger.debug("event forward end.");
			}
		}
		
		public void shutdown(){
			watchOpen = false;
			synchronized(lock){
				lock.notifyAll();
			}
		}
		
		protected void sleep(){
			final Object lock = this.lock;
			synchronized(lock){
				try {
					lock.wait(checkInterval);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		protected boolean checkHealth(){
			try{
				return publisherGroup.getEventTransmission().checkHealth();
			}catch(Exception e){
				logger.error(new StringBuilder("checkHealth failure:").append(e.getMessage()));
				return false;
			}
		}
		
	}

}
