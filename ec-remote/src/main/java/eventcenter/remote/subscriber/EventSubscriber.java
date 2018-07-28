package eventcenter.remote.subscriber;

import eventcenter.api.*;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.SubscriberGroup;
import eventcenter.remote.Target;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 事件订阅者，在dubbo的实现中，需要将asyncTransmission设置为异步调用的接口，在订阅者中，他是一个dubbo的消费者，也就是服务的
 * 提供者，事件发布者为dubbo的生产者
 * @author JackyLIU
 *
 */
public class EventSubscriber implements EventTransmission, eventcenter.remote.EventSubscriber {

	protected final String id;
	
	/**
	 * 事件订阅者服务点的事件中心，一般使用api默认的{@link DefaultEventCenter}即可
	 */
	protected EventCenter eventCenter;

	protected List<SubscribFilter> subscribFilters;

	protected List<SubscriberStartupFilter> startupFilters;

	protected final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 按照group名称建立map
	 */
	protected Map<String, SubscriberGroup> subscriberGroups;
	
	public EventSubscriber(EventCenter eventCenter){
		this();
		this.eventCenter = eventCenter;
	}

	public EventSubscriber(){
		this.id = UUID.randomUUID().toString();
	}
	
	public EventCenter getEventCenter() {
		return eventCenter;
	}

	public void setEventCenter(EventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}

	@PostConstruct
	public void startup(){
		for(SubscriberStartupFilter filter : getStartupFilters()){
			try{
				filter.onStartup(getSubscriberGroups());
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void asyncTransmission(Target target, EventInfo eventInfo,
			Object result) {
		if(ConfigContext.getConfig().isOpenLoggerMdc() && StringHelper.isNotEmpty(eventInfo.getMdcValue())){
			MDC.put(ConfigContext.getConfig().getLoggerMdcField(), eventInfo.getMdcValue());
		}
		if(logger.isDebugEnabled()){
			logger.debug(new StringBuilder("received event:").append(eventInfo).append(" from ").append(target.getTargetClass()).append(" fired"));
		}
		if(filter(target, eventInfo, result)){
			eventCenter.fireEvent(target, eventInfo, result);
		}else if(logger.isTraceEnabled()){
			logger.trace("subscriber refused event from:" + target!=null?target.getNodeId():"" + ", event:" + eventInfo);
		}
		if(ConfigContext.getConfig().isOpenLoggerMdc() && StringHelper.isNotEmpty(eventInfo.getMdcValue())){
			MDC.remove(ConfigContext.getConfig().getLoggerMdcField());
		}
	}

	@Override
	public boolean checkHealth() {
		return true;
	}

	@Override
	public SubscriberGroup getSubscriberGroup(String group) {
		return getSubscriberGroups().get(group);
	}
	
	protected Map<String, SubscriberGroup> getSubscriberGroups(){
		if(null == subscriberGroups)
			subscriberGroups = new HashMap<String, SubscriberGroup>();
		return subscriberGroups;
	}

	public void setSubscriberGroups(List<SubscriberGroup> subscriberGroups) {
		for(SubscriberGroup group : subscriberGroups){
			if(group.getGroupName() == null || group.getGroupName().length() == 0)
				throw new IllegalArgumentException("please set groupName");
			getSubscriberGroups().put(group.getGroupName(), group);
		}
	}

	private boolean filter(Target target, EventInfo eventInfo, Object result){
		List<SubscribFilter> subscriberFilters = getSubscriberFilters();
		if(subscriberFilters.size() == 0)
			return true;
		for(SubscribFilter filter : subscriberFilters){
			try {
				if(!filter.afterReceived(target, eventInfo, result)){
					return false;
				}
			}catch(Throwable t){
				logger.error("handle subscriber filter error:" + t.getMessage(), t);
			}
		}
		return true;
	}

	private List<SubscribFilter> getSubscriberFilters(){
		if(null != subscribFilters)
			return subscribFilters;
		subscribFilters = new ArrayList<SubscribFilter>();
		EventCenterConfig config = ConfigContext.getConfig();
		if(null == config)
			return subscribFilters;
		for(EventFilter filter : config.getModuleFilters()){
			if(filter instanceof SubscribFilter){
				subscribFilters.add((SubscribFilter)filter);
			}
		}
		return subscribFilters;
	}

	private List<SubscriberStartupFilter> getStartupFilters(){
		if(null != startupFilters)
			return startupFilters;
		startupFilters = new ArrayList<SubscriberStartupFilter>();
		EventCenterConfig config = ConfigContext.getConfig();
		if(null == config)
			return startupFilters;
		for(EventFilter filter : config.getModuleFilters()){
			if(filter instanceof SubscriberStartupFilter){
				startupFilters.add((SubscriberStartupFilter)filter);
			}
		}
		return startupFilters;
	}

}
