package eventcenter.api;

import eventcenter.api.async.simple.SimpleQueueEventContainerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * 事件中心的基类
 * @author JackyLIU
 *
 */
public abstract class AbstractEventCenter implements EventCenter, ApplicationContextAware {

	protected EventCenterConfig ecConfig;

	protected List<EventFireFilter> eventFireFilters;
	
	protected ApplicationContext applicationContext;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public EventCenterConfig getEcConfig() {
		if(null == ecConfig){
			ecConfig = new EventCenterConfig();
		}
		return ecConfig;
	}

	public void setEcConfig(EventCenterConfig ecConfig) {
		this.ecConfig = ecConfig;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@PostConstruct
	public void startup() throws Exception{
		if(ecConfig == null && applicationContext != null){
			try{
				ecConfig = applicationContext.getBean(EventCenterConfig.class);
			}catch(NoSuchBeanDefinitionException e){
				// 使用默认的EventCenterConfig
			}
		}
		if(ecConfig == null){
			ecConfig = new EventCenterConfig();
		}
		if(null == ecConfig.getQueueEventContainerFactory()){
			// 创建默认的队列事件容器工厂
			ecConfig.setQueueEventContainerFactory(new SimpleQueueEventContainerFactory());
		}
		if(null != applicationContext){
			Map<String, EventListener> listenerMap = this.applicationContext.getBeansOfType(EventListener.class);
			CommonEventListenerConfig config = ecConfig.convertListenerBind(listenerMap);		
			ecConfig.loadCommonEventListenerConfig(config);

			Map<String, ListenerFilter> filterMap = this.applicationContext.getBeansOfType(ListenerFilter.class);
			ecConfig.loadListenerFilter(filterMap);

			Map<String, EventFilter> otherFilterMap = this.applicationContext.getBeansOfType(EventFilter.class);
			ecConfig.loadFilter(otherFilterMap);
		}
		
		if(null != ecConfig.getEventListenerConfig() && ecConfig.getEventListenerConfig().getListeners().size() > 0){
			ecConfig.loadCommonEventListenerConfig(ecConfig.getEventListenerConfig());
		}
		ecConfig.getAsyncContainer().startup();
		ConfigContext.setEventCenterConfig(ecConfig);
		loadEventService();
	}
	
	@PreDestroy
	public void shutdown() throws Exception{
		if(ecConfig.getAsyncContainer() == null) {
			return ;
		}
		if(ecConfig.getEventServices() != null){
			for(EventService eventService : ecConfig.getEventServices()){
				eventService.shutdown();
				if(logger.isDebugEnabled()){
					logger.debug(new StringBuilder("shutdown eventService:").append(eventService.getClass()).append(" success."));
				}
			}
		}
		ecConfig.getAsyncContainer().shutdown();
	}

	@Override
	public EventRegister findEventRegister(String name) {
		return ecConfig.getEventRegisters().get(name);
	}

	public EventContainer getAsyncContainer() {
		return ecConfig.getAsyncContainer();
	}
	
	public EventCenterConfig getEventCenterConfig(){
		return this.ecConfig;
	}
	
	protected abstract long getDelay(EventInfo eventInfo, EventListener listener);
	
	protected void executeAsyncListeners(Object target, EventInfo eventInfo, Object result, EventRegister register){
		if(null == getAsyncContainer()) {
			throw new NullPointerException("无法在事件中心找到异步事件发送容器,asyncContainer==null");
		}

		try{
			getAsyncContainer().send(createEventSource(register, target, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), result, getMdcValue(eventInfo)));
		}catch(Exception e){
			logger.error(new StringBuilder("发送异步事件异常：").append(e.getMessage()), e);
		}
	}

	protected Object executeSyncListeners(Object target, EventInfo eventInfo, Object result, EventRegister register, List<EventListener> listeners){
		Object __result = null;
		// 最后执行同步事件
		if(listeners.size() > 0){
			for(EventListener listener : listeners){
				try{
					CommonEventSource evt = createEventSource(register, target, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), result, getMdcValue(eventInfo));
					listener.onObserved(evt);
					__result = evt.getSyncResult();
				}catch(Exception e){
					logger.error(new StringBuilder("发送同步事件异常：").append(e.getMessage()), e);
				}
			}
		}

		return __result;
	}

	protected CommonEventSource createEventSource(EventRegister register, Object target, String eventId, String eventName, Object[] args, Object result, String mdcValue){
		return register.createEventSource(target, eventId, eventName, args, result, mdcValue);
	}

	protected String getMdcValue(EventInfo eventInfo){
		if(null != eventInfo && null != eventInfo.getMdcValue()) {
			return eventInfo.getMdcValue();
		}
		if(ecConfig.isOpenLoggerMdc() && ecConfig.getLoggerMdcField() != null){
			Object val = MDC.get(ecConfig.getLoggerMdcField());
			if(null != val){
				return val.toString();
			}
		}
		return null;
	}

	/**
	 * 加载EventService服务接口
	 * @throws Exception
	 */
	void loadEventService() throws Exception {
		// 重新查找下Spring所管理的EventService，并load到ecConfig.eventServices
		if(this.applicationContext != null){
			Set<Integer> serviceHashcodes = new HashSet<Integer>();
			List<EventService> eventServices = new ArrayList<EventService>();
			if(ecConfig.getEventServices() != null){
				eventServices.addAll(ecConfig.getEventServices());
			}
			Map<String, EventService> beansOfType = applicationContext.getBeansOfType(EventService.class);
			// load ecConfig.eventServices into serviceHashcodes
			for(EventService eventService : eventServices){
				serviceHashcodes.add(eventService.hashCode());
			}
			Collection<EventService> values = beansOfType.values();
			for(EventService bean : values){
				if(!serviceHashcodes.contains(bean.hashCode())){
					eventServices.add(bean);
				}
			}
			ecConfig.setEventServices(eventServices);
		}

		if(ecConfig.getEventServices() != null){
			for(EventService eventService : ecConfig.getEventServices()){
				eventService.startup(ecConfig);
				if(logger.isDebugEnabled()){
					logger.debug(new StringBuilder("startup eventService:").append(eventService.getClass()).append(" success."));
				}
			}
		}
	}

	/**
	 * 调用IEventFireFilter拦截器
	 */
	protected void filterEventFire(Object target, EventInfo eventInfo, Object result){
		try{
			if(null != target && target instanceof Remotable) {
				return ;
			}
			List<EventFireFilter> eventFireFilters = getEventFireFilters();
			if(eventFireFilters.size() == 0) {
				return ;
			}
			if(null == eventInfo.getMdcValue()){
				String mdcValue = getMdcValue(eventInfo);
				eventInfo.setMdcValue(mdcValue);
			}
			long start = System.currentTimeMillis();
			for(EventFireFilter fireFilter : eventFireFilters){
				fireFilter.onFired(target, eventInfo, result);
			}
			if(logger.isTraceEnabled()){
				logger.trace("event filter executed, took:" + (System.currentTimeMillis() - start) + " ms. for " + eventInfo.getName());
			}
		}catch (Throwable e){
			logger.error("filter event fired error:" + e.getMessage(), e);
		}
	}

	/**
	 * 这个方法必须要在{@link this#startup()}方法之后调用
	 *
	 * @return
	 */
	protected List<EventFireFilter> getEventFireFilters() {
		if (null == eventFireFilters) {
			synchronized (this) {
				if (null == eventFireFilters) {
					eventFireFilters = new ArrayList<EventFireFilter>();
					for (EventFilter filter : ecConfig.getModuleFilters()) {
						if (filter instanceof EventFireFilter) {
							eventFireFilters.add((EventFireFilter) filter);
						}
					}
				}
			}
		}
		return eventFireFilters;
	}

	@Override
	public Object fireEvent(Object target, String eventName, Object... args) {
		return fireEvent(target, new EventInfo(eventName).setArgs(args));
	}

	@Override
	public Object fireEvent(Object target, EventInfo eventInfo) {
		return fireEvent(target, eventInfo, null);
	}
}
