package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.remote.EventPublisher;
import eventcenter.remote.Target;
import eventcenter.remote.EventPublisher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 将异步事件发布到远程订阅点执行，同步的事件依然使用当前虚拟机中的订阅事件。
 * <p>
 * 首先判断remoteEvents中是否有注册，如果默认事件名称在这里注册了，那么将这个事件发布到远程点中, remoteEvents支持'*'通配符，
 * 例如. 'trade.*'，那么表示以trade.开头的所有事件名称都将发布到远程点中
 * 
 * @author JackyLIU
 *
 */
public class PublishEventCenter extends DefaultEventCenter {
	
	protected EventPublisher eventPublisher;
	
	/**
	 * 发送到远程接收端的策略
	 */
	protected AbstractFireRemoteEventsPolicy fireRemoteEventsPolicy;
	
	/**
	 * 是否使用异步触发事件
	 */
	protected boolean asyncFireRemote = false;

	public EventPublisher getEventPublisher() {
		if(eventPublisher == null)
			eventPublisher = new DefaultEventPublisher();
		return eventPublisher;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		if(null != this.eventPublisher && this.eventPublisher instanceof DefaultEventPublisher){
			// 如果不为空，则需要将已经注入到eventPublisher的重新注入到当前最新的publisher中
			eventPublisher.publish(this.eventPublisher.getPublisherGroups());
		}
		this.eventPublisher = eventPublisher;
	}

	public List<PublisherGroup> getPublisherGroups() {
		return getEventPublisher().getPublisherGroups();
	}

	public void setPublisherGroups(List<PublisherGroup> publisherGroups) {
		getEventPublisher().publish(publisherGroups);
	}
	
	@Override
	@PostConstruct
	public void startup() throws Exception {
		super.startup();
		getEventPublisher().startup();
	}
	
	@PreDestroy
	@Override
	public void shutdown() throws Exception {
		super.shutdown();
		getEventPublisher().shutdown();
	}

	@Override
	public Object fireEvent(Object target, EventInfo eventInfo, Object result) {
		try{
			if(target instanceof Target){
				return _superFireEvent(target, eventInfo, result);
			}
			return __fireEvent(target, eventInfo, result);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	Object _superFireEvent(Object target, EventInfo eventInfo,
			Object result){
		return super.fireEvent(target, eventInfo, result);
	}

	Object __fireEvent(Object target, EventInfo eventInfo,
			Object result) {
		List<PublisherGroup> groups = getPublisherGroups();
		if (groups.size() == 0) {
			// 如果没有远程事件，则执行本地事件
			return _superFireEvent(target, eventInfo, result);
		}

		List<PublisherGroup> readyToSend = new ArrayList<PublisherGroup>();
		LocalPublisherGroup localGroup = null;
		for (PublisherGroup group : groups) {
			if (group.isRemoteEvent(eventInfo.getName())) {
				if(group instanceof LocalPublisherGroup){
					localGroup = (LocalPublisherGroup)group;
					continue;
				}
				readyToSend.add(group);
			}
		}

		if (readyToSend.size() == 0) {
			// 如果没有远程事件，则执行本地事件
			return _superFireEvent(target, eventInfo, result);
		} 
		
		try {
			final boolean filterEventFire = localGroup == null;
			fireRemoteEvent(readyToSend, target, eventInfo, result, filterEventFire);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		if(localGroup != null){
			try{
				return _superFireEvent(target, eventInfo, result);
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}

		return null;
	}
	
	protected void handleAsyncTransmissionException(Exception e, PublisherGroup group, Object target, EventInfo eventInfo, Object result){
		// 暂时不处理
	}

	protected void fireRemoteEvent(List<PublisherGroup> groups, Object target,
			EventInfo eventInfo, Object result, boolean filterEventFire) {
		if(filterEventFire) {
			filterEventFire(target, eventInfo, result);
		}
		Target __target = new Target(target.getClass().getName());
		eventInfo.setMdcValue(getMdcValue(eventInfo));
		getFireRemoteEventsPolicy().fireRemoteEvents(groups, __target, eventInfo, result);
	}

	public AbstractFireRemoteEventsPolicy getFireRemoteEventsPolicy() {
		if(null == fireRemoteEventsPolicy)
			fireRemoteEventsPolicy = createAbstractFireRemoteEventsPolicy();
		return fireRemoteEventsPolicy;
	}
	
	private AbstractFireRemoteEventsPolicy createAbstractFireRemoteEventsPolicy(){
		if(asyncFireRemote)
			return new AsyncFireRemoteEventsPolicy(this);
		return new DefaultFireRemoteEventsPolicy(this);
	}

	public boolean isAsyncFireRemote() {
		return asyncFireRemote;
	}

	public void setAsyncFireRemote(boolean asyncFireRemote) {
		this.asyncFireRemote = asyncFireRemote;
	}
}
