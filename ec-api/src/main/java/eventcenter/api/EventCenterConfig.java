package eventcenter.api;

import eventcenter.api.annotation.EventFilterable;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.QueueEventContainerFactory;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.async.simple.SimpleEventQueue;
import eventcenter.api.async.simple.SimpleQueueEventContainer;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.QueueEventContainer;
import eventcenter.api.async.QueueEventContainerFactory;
import eventcenter.api.async.simple.SimpleEventQueue;
import eventcenter.api.async.simple.SimpleQueueEventContainer;
import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * 事件中心配置
 * @author JackyLIU
 *
 */
public class EventCenterConfig {

	private Map<String, EventRegister> eventRegisters;
	
	/**
	 * 异步事件发送容器，外部不能直接设置这个容器，需要使用{@link QueueEventContainerFactory}构建出来
	 */
	private QueueEventContainer asyncContainer;
	
	/**
	 * 创建{@link QueueEventContainer}容器的工厂
	 */
	private QueueEventContainerFactory queueEventContainerFactory;
	
	/**
	 * 使用{@link CommonEventRegister}的类作为监听器的配置容器，那么只需要配置监听器即可
	 */
	private CommonEventListenerConfig eventListenerConfig;
	
	/**
	 * 为asyncContainer设置队列的容量，如果使用的是VM内部的队列，这个参数有效
	 */
	private Integer queueCapacity;

	/**
	 * 是否开启日志mdc的追踪
	 */
	private boolean openLoggerMdc = false;

	/**
	 * 开启openLoggerMdc之后，还需要设置追踪的字段
	 */
	private String loggerMdcField;

	private List<EventService> eventServices;

	/**
	 * global listener filters
	 */
	private List<ListenerFilter> globalFilters;

	/**
	 * 模块过滤器，例如ec-remote中会使用到IPublishFilter和ISubscribFilter这两个过滤器
	 */
	private List<EventFilter> moduleFilters;

	/**
	 * map some events to some filters
	 */
	private Map<String, List<ListenerFilter>> listenerFilters;

	public boolean isOpenLoggerMdc() {
		return openLoggerMdc;
	}

	public String getLoggerMdcField() {
		return loggerMdcField;
	}

	public void setOpenLoggerMdc(boolean openLoggerMdc) {
		this.openLoggerMdc = openLoggerMdc;
	}

	public void setLoggerMdcField(String loggerMdcField) {
		this.loggerMdcField = loggerMdcField;
	}

	private final Logger logger = Logger.getLogger(this.getClass());

	public Map<String, EventRegister> getEventRegisters() {
		if(null == eventRegisters)
			eventRegisters = new HashMap<String, EventRegister>();
		return eventRegisters;
	}

	public void setEventRegisters(Map<String, EventRegister> eventRegisters) {
		this.eventRegisters = eventRegisters;
	}
	
	public void setAsyncContainer(QueueEventContainer asyncContainer){
		this.asyncContainer = asyncContainer;
	}

	public List<EventService> getEventServices() {
		return eventServices;
	}

	public void setEventServices(List<EventService> eventServices) {
		this.eventServices = eventServices;
	}

	private EventQueue createDefaultEventQueue(){
		if(null == queueCapacity)
			return new SimpleEventQueue();
		return new SimpleEventQueue(queueCapacity);
	}

	public QueueEventContainerFactory getQueueEventContainerFactory() {
		return queueEventContainerFactory;
	}

	public void setQueueEventContainerFactory(
			QueueEventContainerFactory queueEventContainerFactory) {
		this.queueEventContainerFactory = queueEventContainerFactory;
	}

	public QueueEventContainer getAsyncContainer() {
		if(null == asyncContainer && null == queueEventContainerFactory){
			asyncContainer = new SimpleQueueEventContainer(this, createDefaultEventQueue());
			/*try {
				asyncContainer.startup();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}*/
		}else if(null == asyncContainer){
			asyncContainer = queueEventContainerFactory.createContainer(this);
		}
		return asyncContainer;
	}

	public CommonEventListenerConfig getEventListenerConfig() {
		return eventListenerConfig;
	}

	public Integer getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(Integer queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public List<ListenerFilter> getGlobalFilters() {
		if(null == globalFilters)
			globalFilters = new ArrayList<ListenerFilter>();
		return globalFilters;
	}

	public void setGlobalFilters(List<ListenerFilter> globalFilters) {
		this.globalFilters = globalFilters;
	}

	public Map<String, List<ListenerFilter>> getListenerFilters() {
		if(null == listenerFilters)
			listenerFilters = new HashMap<String, List<ListenerFilter>>();
		return listenerFilters;
	}

	public void setListenerFilters(Map<String, List<ListenerFilter>> listenerFilters) {
		this.listenerFilters = listenerFilters;
	}

	/**
	 * 加载eventListenerConfig时，将会做初始化的操作
	 * @param eventListenerConfig
	 */
	public void setEventListenerConfig(CommonEventListenerConfig eventListenerConfig) {
		this.eventListenerConfig = eventListenerConfig;
	}

	/**
	 * 首先扫描listenerMap中的IEventListener的实现类中是否加载了{@link ListenerBind}注解，如果加载了则生成到CommonEventListenerConfig中
	 * @param listenerMap
	 * @return
	 */
	CommonEventListenerConfig convertListenerBind(Map<String, EventListener> listenerMap){
		Collection<EventListener> listeners = listenerMap.values();
		CommonEventListenerConfig config = new CommonEventListenerConfig();
		Map<String, List<EventListener>> eventListeners = new HashMap<String, List<EventListener>>();
		for(EventListener listener : listeners){
			try{
				ListenerBind bind = listener.getClass().getAnnotation(ListenerBind.class);
				if(bind == null) {
					// listener有可能被Spring使用了AOP的代理
					Class<?> targetClass = AopUtils.getTargetClass(listener);
					bind = targetClass.getAnnotation(ListenerBind.class);
					if(bind != null){

					}else {
						continue;
					}
				}
				// 这个value可以绑定到多个事件中去
				String[] eventNames = bind.value().split(",");
				for(String eventName : eventNames){
					String __eventName = eventName.trim();
					if(!eventListeners.containsKey(__eventName)){
						eventListeners.put(__eventName, new ArrayList<EventListener>());
					}
					eventListeners.get(__eventName).add(listener);
				}
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
		config.setListeners(eventListeners);
		return config;
	}

	/**
	 * 加载IListenerFilter，这里主要会读取注解{@link eventcenter.api.annotation.EventFilterable}，如果过滤器上没加这个注解，那么将不会加载到过滤器的配置中
	 * @param filterMap
	 */
	void loadListenerFilter(Map<String, ListenerFilter> filterMap){
		if(null == filterMap || filterMap.size() == 0)
			return ;

		Collection<ListenerFilter> filters = filterMap.values();
		for(ListenerFilter filter : filters){
			EventFilterable ef = filter.getClass().getAnnotation(EventFilterable.class);
			if(null == ef)
				continue ;
			if(ef.isGlobal()){
				getGlobalFilters().add(filter);
				if(logger.isDebugEnabled()){
					logger.debug(new StringBuilder("load global event filter:").append(filter.getClass().toString()));
				}
				continue;
			}
			if(ef.value() == null)
				throw new IllegalArgumentException("please set value for EventFilterable");
			String[] eventNames = ef.value().split(",");
			for(String eventName : eventNames){
				addFilter(eventName.trim(), filter);
			}
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("load event filter:").append(ef.value()).append(":").append(filter.getClass().toString()));
			}
		}
	}

	/**
	 * 加载IFilter组件，这里会踢出IListenerFilter接口
	 * @param filterMap
	 */
	void loadFilter(Map<String, EventFilter> filterMap){
		if(null == filterMap || filterMap.size() == 0)
			return ;

		Collection<EventFilter> filters = filterMap.values();
		for(EventFilter filter : filters){
			getModuleFilters().add(filter);
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("load other filter:").append(filter.getClass().toString()));
			}
		}
	}
	
	public void loadCommonEventListenerConfig(CommonEventListenerConfig eventListenerConfig){	
		
		if(eventRegisters == null){
			eventRegisters = new HashMap<String, EventRegister>();
		}
		
		for(Entry<String, List<EventListener>> entry : eventListenerConfig.getListeners().entrySet()){
			if(eventRegisters.containsKey(entry.getKey()) && !(eventRegisters.get(entry.getKey()) instanceof CommonEventRegister)){
				// 如果eventRegisters已包含了eventListenerConfig中配置的事件名称，那么肯定已eventRegisters中为主，而且类型不是
				// CommonEventRegister的类型，那么就会抛出异常
				throw new IllegalArgumentException("eventRegisters中已经包含了" + entry.getKey() + "，不能直接将他注册到CommonEventRegister类型中。");
			}else if(eventRegisters.containsKey(entry.getKey())){
				CommonEventRegister register = (CommonEventRegister)eventRegisters.get(entry.getKey());
				entry.getValue().addAll(Arrays.asList(register.getEventListeners()));
				register.setEventListeners(entry.getValue().toArray(new EventListener[entry.getValue().size()]));
				return ;
			}
			
			CommonEventRegister register = new CommonEventRegister();
			register.setEventListeners(entry.getValue().toArray(new EventListener[entry.getValue().size()]));
			
			eventRegisters.put(entry.getKey(), register);
			if(logger.isDebugEnabled()){
				StringBuilder sb = new StringBuilder(new StringBuilder("load event listener:").append(entry.getKey()).append(":"));
				int index = 0;
				for(EventListener listener : register.getEventListeners()){
					if(index > 0){
						sb.append(",");
					}
					sb.append(listener.getClass().toString());
					index++;
				}
				logger.debug(sb);
			}
		}
	}

	/**
	 * 添加过滤器，非全局的过滤器，所以需要设置eventName
	 * @param eventName
	 * @param filter
	 */
	public void addFilter(String eventName, ListenerFilter filter){
		if(null == eventName || "".equals(eventName))
			throw new IllegalArgumentException("please set eventName to add ListenerFilter");

		List<ListenerFilter> filters = getListenerFilters().get(eventName);
		if(null == filters)
			filters = new ArrayList<ListenerFilter>();

		filters.add(filter);
		getListenerFilters().put(eventName, filters);
	}

	/**
	 * 直接调用加载{@link EventListener}
	 * @param eventName
	 * @param listener
	 */
	public void putCommonListener(String eventName, EventListener listener){
		List<EventListener> registedListeners = getCommonListenerConfig().getListeners().get(eventName);
		if(null == registedListeners){
			registedListeners = new ArrayList<EventListener>();
			getCommonListenerConfig().getListeners().put(eventName, registedListeners);
		}
		registedListeners.add(listener);
	}

	private CommonEventListenerConfig getCommonListenerConfig(){
		if(null == this.eventListenerConfig)
			this.eventListenerConfig = new CommonEventListenerConfig();
		return this.eventListenerConfig;
	}

	public List<EventFilter> getModuleFilters() {
		if(null == moduleFilters){
			moduleFilters = new ArrayList<EventFilter>();
		}
		return moduleFilters;
	}

	public void setModuleFilters(List<EventFilter> moduleFilters) {
		this.moduleFilters = moduleFilters;
	}
}
