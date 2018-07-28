package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.api.EventFilter;
import eventcenter.api.appcache.IdentifyContext;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.Target;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFireRemoteEventsPolicy {
	
	protected final PublishEventCenter eventCenter;

	private List<PublishFilter> publishFilters;

	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public AbstractFireRemoteEventsPolicy(PublishEventCenter eventCenter){
		this.eventCenter = eventCenter;
	}

	/**
	 * 发送事件到远程接收端
	 * @param groups
	 * @param target
	 * @param eventInfo
	 * @param result
	 */
	public abstract void fireRemoteEvents(List<PublisherGroup> groups, Target target,EventInfo eventInfo, Object result);
	
	/**
	 * 当发送到一个点报错，那么将触发这个方法
	 * @param e
	 * @param group
	 * @param target
	 * @param eventInfo
	 * @param result
	 */
	public void handleAsyncTransmissionException(Exception e, PublisherGroup group, Target target, EventInfo eventInfo, Object result){
		filterAfter(group, target, eventInfo, result, e);
		eventCenter.handleAsyncTransmissionException(e, group, target, eventInfo, result);
	}

	/**
	 * 派发事件到远程消费端
	 */
	public void asyncTransmission(PublisherGroup group, Target target, EventInfo eventInfo, Object result) throws Exception {
		try {
			asyncTransmissionDirectly(group.getEventTransmission(), group, target, eventInfo, result);
		} catch (Exception e) {
			logger.error(
					new StringBuilder("send remote event:")
							.append(eventInfo).append(",remoteAddress:").append(group.getRemoteUrl()).append(" error:")
							.append(e.getMessage()), e);
			handleAsyncTransmissionException(e, group, target, eventInfo, result);
			throw e;
		}
	}

	public void asyncTransmissionDirectly(EventTransmission eventTransmission, PublisherGroup group, Target target, EventInfo eventInfo, Object result){
		if(null == target.getNodeId()){
			try {
				target.setNodeId(IdentifyContext.getId());
			} catch (IOException e) {
				logger.error("IdentifyContext getId error:" + e.getMessage());
			}
		}
		eventTransmission.asyncTransmission(target,
				eventInfo, result);
		if(logger.isDebugEnabled()){
			if(group == null || group.getGroupName() == null){
				logger.debug(new StringBuilder("send remote event to ").append(eventInfo));
			}else{
				logger.debug(new StringBuilder("send remote event to ").append(eventInfo).append(", group: ").append(group.getGroupName()).append(", remoteAddress:").append(group.getRemoteUrl()));
			}

		}
		filterAfter(group, target, eventInfo, result, null);
	}

	/**
	 * find publish filters from ecConfig's module filters
	 * @return
	 */
	protected List<PublishFilter> getPublishFilters(){
		if(null != publishFilters)
			return publishFilters;
		publishFilters = new ArrayList<PublishFilter>();
		for(EventFilter filter : eventCenter.getEcConfig().getModuleFilters()){
			if(filter instanceof PublishFilter){
				publishFilters.add((PublishFilter)filter);
			}
		}
		return publishFilters;
	}

	protected void filterAfter(PublisherGroup group, Target target, EventInfo eventInfo, Object result, Exception e){
		if(null == group){
			// TODO 如果group为空，应该需要通过IEventTransmission找到这个group
			return ;
		}
		List<PublishFilter> publishFilters = getPublishFilters();
		if(publishFilters.size() == 0)
			return ;

		for(PublishFilter filter : publishFilters){
			try {
				if(!filter.afterSend(group, target, eventInfo, result, e)){
					break;
				}
			}catch(Throwable t){
				logger.error("publish filter executed failure, filter:" + filter.getClass().getName() + ", error:" + e.getMessage());
			}
		}
	}
}
