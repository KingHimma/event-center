package eventcenter.remote.saf;

import eventcenter.api.EventInfo;
import eventcenter.api.async.EventQueue;
import eventcenter.remote.EventInfoSource;
import eventcenter.remote.Target;
import eventcenter.remote.publisher.PublishEventCenter;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.saf.simple.SimpleStoreAndForwardPolicy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 具有SAF特性的远程事件发布中心
 * @author JackyLIU
 *
 */
public class SAFPublishEventCenter extends PublishEventCenter {

	private StoreAndForwardPolicy safPolicy;
	
	private volatile boolean startup = false;
	
	EventForward eventForward;
	
	public StoreAndForwardPolicy getSafPolicy() {
		return safPolicy;
	}

	public void setSafPolicy(StoreAndForwardPolicy safPolicy) {
		this.safPolicy = safPolicy;
	}
	
	@Override
	@PostConstruct
	public void startup() throws Exception{
		if(startup) {
			return ;
		}
		long start = System.currentTimeMillis();
		if(null == safPolicy){
			safPolicy = new SimpleStoreAndForwardPolicy();
		}
		Map<PublisherGroup, EventQueue> monitors = new HashMap<PublisherGroup, EventQueue>();
		for(PublisherGroup group : getPublisherGroups()){
			if(group.getEventTransmission() != null) {
				// TODO 如果是广播的方式进行订阅事件，那么不同的模块的groupName是相同的，这里会有问题
				monitors.put(group, safPolicy.createEventQueue(group.getGroupName()));
			}
		}
		
		eventForward = safPolicy.createEventForward();
		eventForward.setFireRemoteEventsPolicy(getFireRemoteEventsPolicy());
		eventForward.startup(monitors);
		if(getEventPublisher() instanceof PublishGroupChangeable){
			((PublishGroupChangeable)getEventPublisher()).setForwardAndStorePolicy(eventForward, safPolicy);
		}
		super.startup();
		startup = true;
		if(logger.isDebugEnabled()){
			logger.debug("startup SAFPublishEventCenter success. took: " + (System.currentTimeMillis() - start) + " ms.");
		}
	}
	
	@Override
	@PreDestroy
	public void shutdown() throws Exception{
		if(!startup) {
			return ;
		}
		startup = false;
		super.shutdown();
		eventForward.shutdown();
	}

	@Override
	protected void fireRemoteEvent(List<PublisherGroup> groups, Object target,
			EventInfo eventInfo, Object result, boolean filterEventFire) {
		// 如果是发生异常再储存事件，则直接调用父类的远程事件方法
		if(safPolicy.storeOnSendFail()){
			super.fireRemoteEvent(groups, target, eventInfo, result, filterEventFire);
			return ;
		}

		if(filterEventFire) {
			filterEventFire(target, eventInfo, result);
		}
		// 一般是先发送到队列中，然后由IEventForward进行推送
		batchInQueue(groups, target, eventInfo, result);
	}
	
	@Override
	protected void handleAsyncTransmissionException(Exception e,
			PublisherGroup group, Object target, EventInfo eventInfo,
			Object result) {
		singleInQueue(group, target, eventInfo, result);
	}

	private void batchInQueue(List<PublisherGroup> groups, Object target,
			EventInfo eventInfo, Object result){
		for(PublisherGroup group : groups){
			singleInQueue(group, target, eventInfo, result);
		}
	}
	
	private void singleInQueue(PublisherGroup group, Object target, EventInfo eventInfo, Object result){
		try{
			eventForward.getMonitors().get(group).offer(createEventInfoSource(target, eventInfo, result));
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
	
	private EventInfoSource createEventInfoSource(Object target, EventInfo eventInfo, Object result){
		EventInfoSource source = new EventInfoSource(target, eventInfo, result);
		source.setTarget(new Target(target.getClass().getName()));
		return source;
	}
}
