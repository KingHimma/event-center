package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import eventcenter.remote.EventPublisher;
import eventcenter.remote.EventSubscriber;
import eventcenter.remote.SubscriberGroup;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.saf.EventForward;
import eventcenter.remote.saf.PublishGroupChangeable;
import eventcenter.remote.saf.StoreAndForwardPolicy;
import eventcenter.remote.utils.ExpiryMap;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用zookeeper获取事件订阅者，这个需要依靠dubbo的{@link RegistryService}获取
 * @author JackyLIU
 *
 */
public class DubboRegistryEventPublisher implements EventPublisher, PublishGroupChangeable, ApplicationContextAware {
	
    public static final String REGISTRY_ADDRESS = "dubbo.registry.address";
    
    public static final String APPLICATION_NAME = "dubbo.application.name";

	public static final String APP_DATA_NAME = "dubbo.saf.offline";
    
	/**
	 * 需要监听zk的dubbo的group名称，默认为所有的分组
	 */
	private String dubboGroup;
	
	@Autowired(required=false)
    private RegistryService registryService;
	
	private ApplicationContext applicationContext;
	
	/**
	 * 如果远程dubbo的版本号和分组信息都是一致时，并且存在多个点时，事件将会远程发布到每一个点钟。如果为false，那么默认使用
	 * dubbo自己的负载方式进行访问
	 */
	private boolean copySendUnderSameVersion = false;
	
	/**
	 * 是否开启开发模式，当开启后，事件将只发布到当前机器的订阅者
	 */
	private boolean devMode = false;
	
	/**
	 * 远程注册的事件，如果在这个subscribEvents找不到，那么需要从serviceProviders获取，key为host:port,
	 */
	// TODO 这里的缓存应该有个时间控制，比如半个小时失效一次，然后再从remoteSubscriberFactories中获取
	protected Map<String, String> subscribEvents = new ConcurrentHashMap<String, String>();
	
	/**
	 * 远程订阅工厂，key为host:port
	 */
	protected Map<String, RemoteSubscriberFactory> remoteSubscriberFactories = new ConcurrentHashMap<String, RemoteSubscriberFactory>();
	
	protected Map<String, EventSubscriber> eventSubscribers = new ConcurrentHashMap<String, EventSubscriber>();
	
	protected Map<String, PublisherGroupFactory> publisherGroupFactories = new ConcurrentHashMap<String, PublisherGroupFactory>();
	
	protected Map<String, PublisherGroup> publisherGroups = new ConcurrentHashMap<String, PublisherGroup>();
	
	protected List<PublisherGroup> localPublisherGroups = null;
	
	protected Map<String, List<URL>> serviceProviders = new ConcurrentHashMap<String, List<URL>>();

	protected Map<String, Date> monitorMap = new ConcurrentHashMap<String, Date>();
	
	/**
	 * 当订阅端离线时，给定一个有效期，如果有效期过了，则删除掉离线队列
	 */
	ExpiryMap<String, URL> garbagePool;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	RegistryConfig registryConfig;

	ApplicationConfig applicationConfig;
	
	/**
	 * 默认有效期为1天
	 */
	private int expiryOffline = 24*3600;
	
	private EventForward eventForward;
	
	private StoreAndForwardPolicy storeAndForwardPolicy;
	
	/**
	 * 本地订阅者的序列号
	 */
	private String localSubscriberId;
	
	/**
	 * 本地的IP+端口
	 */
	private String localAddress;

	/**
	 * dubbo使用的registryUrl，默认会使用dubbo.properties中定义的dubbo.registry.address
	 */
	private String registryUrl;

	private Object locker = new Object();

	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	public boolean isCopySendUnderSameVersion() {
		return copySendUnderSameVersion;
	}

	public void setCopySendUnderSameVersion(boolean copySendUnderSameVersion) {
		this.copySendUnderSameVersion = copySendUnderSameVersion;
	}

	public boolean isDevMode() {
		return devMode;
	}

	public void setDevMode(boolean devMode) {
		this.devMode = devMode;
	}

	public String getDubboGroup() {
		return dubboGroup;
	}

	public void setDubboGroup(String dubboGroup) {
		this.dubboGroup = dubboGroup;
	}
	
	/**
	 * 判断是否需要使用回收池
	 * @return
	 */
	protected boolean needUseGarbagePool(){
		return null != eventForward;
	}

	@Override
	public void startup() {		
		if(null == registryService && applicationContext != null){
			registryService = RegistryServiceFactory.buildDefault(applicationContext);
		}

		// 加载applicationConfig和registryConfig
		if(applicationContext != null) {
			if(null == applicationConfig){
				try {
					applicationConfig = applicationContext.getBean(ApplicationConfig.class);
				} catch (NoSuchBeanDefinitionException e) {
				}
			}
			if(null == registryConfig){
				try {
					registryConfig = applicationContext.getBean(RegistryConfig.class);
				} catch (NoSuchBeanDefinitionException e) {
				}
			}
		}

		if(localSubscriberId == null && applicationContext != null){
			try{
				EventSubscriber localEventSubscriber = applicationContext.getBean(EventSubscriber.class);
				localSubscriberId = localEventSubscriber.getId();
			}catch(Exception e){
				
			}
		}
		
		if(dubboGroup == null || dubboGroup.length() == 0)
			throw new IllegalArgumentException("Please set dubboGroup property");

		initRegistryService();
	}

	ExpiryMap<String, URL> getGarbagePool(){
		if(null != garbagePool){
			return garbagePool;
		}
		synchronized (locker){
			if(null != garbagePool){
				return garbagePool;
			}
			garbagePool = createGarbagePool();
			garbagePool.startup();
			garbagePool.setExpiriedCallback(new ExpiryMap.ExpiriedCallback<String, URL>() {
				@Override
				public void onExpiried(String key, URL value) {

					removeSubscriber(key, value);
				}
			});
			return garbagePool;
		}
	}

	/**
	 * init registry service and subscriber notify
	 */
	void initRegistryService(){
		if(registryConfig == null) {
			String url = StringHelper.isNotEmpty(registryUrl) ? registryUrl : ConfigUtils.getProperty(REGISTRY_ADDRESS);
			if (url == null || url.length() == 0) {
				throw new IllegalArgumentException("Please set java start argument: -D" + REGISTRY_ADDRESS + "=zookeeper://127.0.0.1:2181");
			}

			registryConfig = new RegistryConfig();
			registryConfig.setAddress(url);
			registryConfig.setRegister(false);
		}

		URL eventSubscriberUrl = new URL(Constants.ADMIN_PROTOCOL, NetUtils.getLocalHost(), 0, "",
				Constants.INTERFACE_KEY, EventSubscriber.class.getName(),
				Constants.GROUP_KEY, dubboGroup,
				Constants.VERSION_KEY, Constants.ANY_VALUE,
				Constants.CLASSIFIER_KEY, Constants.ANY_VALUE,
				Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY,
				Constants.CHECK_KEY, String.valueOf(false));
		registryService.subscribe(eventSubscriberUrl, createNotifyListener());
		logger.info("startup dubbo registry success, group:" + dubboGroup);
	}
	
	ExpiryMap<String, URL> createGarbagePool(){
		return new ExpiryMap<String, URL>();
	}
	
	NotifyListener createNotifyListener(){
		return new NotifyListener() {
        	@Override
        	public void notify(List<URL> urls) {
        		_notify(urls);
        	}
        };
	}
	
	protected void _notify(List<URL> urls){
		if (urls == null || urls.size() == 0) {
            return;
        }        		

        Map<String, List<URL>> providerMap = new HashMap<String, List<URL>>();
        for (URL url : urls) {
        	String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        	if (!Constants.PROVIDERS_CATEGORY.equals(category)) {
				continue;
			}
        	
        	String service = url.getServiceInterface();
        	if(!EventSubscriber.class.getName().equals(service)) {
				continue;
			}
        	
        	if (Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
        		destroyAllSubscribers(service);
        		return ;
        	}
        	
        	List<URL> list = providerMap.get(service);
            if (list == null) {
                list = new ArrayList<URL>();
                providerMap.put(service, list);
            }
            list.add(url);
        }
        
        List<URL> offlineProviders = findOfflineProviders(providerMap.get(EventSubscriber.class.getName()));
        serviceProviders.putAll(providerMap);
        if(offlineProviders != null && offlineProviders.size() > 0){
        	// 销毁离线的服务端
        	destroySubscribers(EventSubscriber.class.getName(), offlineProviders);
        }
        loadSubscriber(providerMap);
	}
	
	/**
	 * 找出离线的接口提供者
	 * @param urls
	 * @return
	 */
	protected List<URL> findOfflineProviders(List<URL> urls){
		if(!serviceProviders.containsKey(EventSubscriber.class.getName())){
			return null;
		}
		
		List<URL> localUrls = serviceProviders.get(EventSubscriber.class.getName());
		Map<String, URL> remoteMap = toMap(urls);
		List<URL> offlines = new ArrayList<URL>();
		for(URL localUrl : localUrls){
			if(!remoteMap.containsKey(createAddress(localUrl))){
				offlines.add(localUrl);
			}
		}
		return offlines;
	}
	
	private Map<String, URL> toMap(List<URL> urls){
		Map<String, URL> map = new HashMap<String, URL>(urls.size());
		for(URL url : urls){
			map.put(createAddress(url), url);
		}
		return map;
	}
	
	@Override
	public void shutdown() {
		if(needUseGarbagePool() && garbagePool != null){
			garbagePool.shutdown();
		}
	}
	
	/**
	 * 加载远程订阅者
	 * @param providerMap
	 */
	protected void loadSubscriber(Map<String, List<URL>> providerMap){
		if(providerMap.containsKey(EventSubscriber.class.getName())){
			loadEventSubscriber(providerMap.get(EventSubscriber.class.getName()), null);
		}
	}
	
	private String createAddress(URL url){
		if(copySendUnderSameVersion)
			return new StringBuilder(url.getHost()).append(":").append(url.getPort()).toString();
		
		return url.getParameter("version");
	}
	
	protected void loadEventSubscriber(List<URL> urls, List<String> remoteEventsList){
		if(null == urls || urls.size() == 0)
			return ; 

		for(int i = 0;i < urls.size();i++){
			URL url = urls.get(i);
			String offlineEvents = null;
			if(null != remoteEventsList && remoteEventsList.size() > 0){
				offlineEvents = remoteEventsList.get(i);
			}
			String address = createAddress(url);
			
			if(StringHelper.equals(address, localAddress)){
				continue;
			}

			if(devMode && !filterWithDevMode(url)){
				continue;
			}

			try{
				boolean firstLoad = false;
				RemoteSubscriberFactory factory = remoteSubscriberFactories.get(address);
				if (null == factory) {
					factory = createRemoteSubscriberFactory(url, address);
					remoteSubscriberFactories.put(address, factory);
					firstLoad = true;
				}

				EventSubscriber subscriber = factory.createEventSubscriber();

				try {
					// 过滤本地的订阅器
					if (null != localSubscriberId && localSubscriberId.equals(subscriber.getId())) {
						// 这个表示当前加载的订阅器是本地启动的，这个将会被忽略
						localAddress = address;
						continue;
					}
				}catch(Exception e){

				}

				Date offlineDate = null;
				try {
					eventSubscribers.put(address, subscriber);
					SubscriberGroup group = subscriber.getSubscriberGroup(dubboGroup);
					if (null == group) {
						logger.warn(new StringBuilder(url.toString()).append(" can't find group events!"));
						continue;
					}

					subscribEvents.put(address, group.getRemoteEvents());

					if(needUseGarbagePool() && getGarbagePool().containKey(address)){
						getGarbagePool().remove(address);
						if(logger.isDebugEnabled()){
							logger.debug(new StringBuilder("remove garbagePool:").append(address));
						}
					}
				}catch(Exception e){
					logger.error("load subscriber group failure:" + e.getMessage(), e);
					if(offlineEvents != null){
						subscribEvents.put(address, offlineEvents);
					}
					offlineDate = new Date();
					destroySubscriber(EventSubscriber.class.getName(), url, offlineDate);
				}

				PublisherGroup publisherGroup = loadPublisherGroup(url);

				if(needUseGarbagePool() && publisherGroup != null){
					addMonitor(address, publisherGroup, url, offlineDate);
				}

				if(firstLoad && logger.isDebugEnabled() && publisherGroup != null){
					logger.debug(new StringBuilder("load url:").append(url).append(" success. remote events:").append(publisherGroup.getRemoteEvents()));
				}
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void addMonitor(String address, PublisherGroup publisherGroup, URL url, Date offlineDate){
		if(monitorMap.containsKey(address)){
			if(logger.isTraceEnabled()) {
				logger.trace(new StringBuilder(address).append(" had been add monitor, can't add again!"));
			}
			return ;
		}
		eventForward.addMonitor(publisherGroup, storeAndForwardPolicy.createEventQueue(address));
		monitorMap.put(address, new Date());
		if(logger.isTraceEnabled()){
			logger.trace(new StringBuilder("add monitor for event forward:").append(address));
		}
	}
	
	protected boolean filterWithDevMode(URL url){
		final String remoteAddress = new StringBuilder(url.getHost()).append(":").append(url.getPort()).toString();

		if(remoteAddress.contains("127.0.0.1") || remoteAddress.contains("localhost"))
			return true;
		if(remoteAddress.contains(NetUtils.getLocalHost()))
			return true;
		return false;
	}
	
	RemoteSubscriberFactory createRemoteSubscriberFactory(URL url, String address){
		RemoteSubscriberFactory factory = RemoteSubscriberFactory.buildWith(url, ConfigUtils.getProperty(APPLICATION_NAME), registryConfig, applicationConfig);
		if(devMode || copySendUnderSameVersion){
			if(copySendUnderSameVersion){
				factory.getReferenceConfig().setUrl(buildDubboUrl(address));
			}
			else{
				factory.getReferenceConfig().setUrl(buildDubboUrl(new StringBuilder(url.getHost()).append(":").append(url.getPort()).toString()));
			}
		}
		return factory;
	}
	
	String buildDubboUrl(String address){
		return new StringBuilder("dubbo://").append(address).toString();
	}
	
	protected PublisherGroup loadPublisherGroup(URL url){
		String address = createAddress(url);

		PublisherGroupFactory factory = publisherGroupFactories.get(address);
		if (null == factory) {
			factory = createPublisherGroupFactory(url, address);
			publisherGroupFactories.put(address, factory);
		} else {
			if (needUpdatePublisherGroupFactory(factory, address)) {
				updatePublisherGroupFactory(factory, address);
			}
		}

		try{
			PublisherGroup group = factory.createPublisherGroup();
			group.setRemoteUrl(url.getAddress());
			group.setGroupName(buildGroupName(url));
			publisherGroups.put(address, group);
			if(logger.isTraceEnabled()){
				logger.trace(new StringBuilder("load url:").append(url));
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}

		return publisherGroups.get(address);
	}

	private boolean needUpdatePublisherGroupFactory(PublisherGroupFactory factory, String address) {
		return !StringUtils.isEquals(factory.getRemoteEvents(), subscribEvents.get(address));
	}

	private void updatePublisherGroupFactory(PublisherGroupFactory factory, String address) {
		factory.setRemoteEvents(subscribEvents.get(address));
	}

	/**
	 * 构建publishGroup的groupName，格式为group^application
	 * @param url
	 * @return
	 */
	String buildGroupName(URL url){
		return new StringBuilder(url.getParameter("group")).append("_").append(url.getParameter("application")).toString();
	}

	PublisherGroupFactory createPublisherGroupFactory(URL url, String address){
		if(this.copySendUnderSameVersion){
			if(null != applicationConfig){
				return PublisherGroupFactory.buildWith(url, subscribEvents.get(address), applicationConfig);
			}
			return PublisherGroupFactory.buildWith(url, subscribEvents.get(address), ConfigUtils.getProperty(APPLICATION_NAME));
		}else{
			if(null != applicationConfig){
				return PublisherGroupFactory.buildWith(url, subscribEvents.get(address), applicationConfig, registryConfig);
			}
			return PublisherGroupFactory.buildWith(url, subscribEvents.get(address), ConfigUtils.getProperty(APPLICATION_NAME), registryConfig);
		}
	}
	
	protected void destroySubscribers(String service, List<URL> urls){
		for(URL url : urls){
			destroySubscriber(service, url, new Date());
		}
	}
	
	/**
	 * 销毁远程订阅者
	 * @param service
	 * @param url
	 */
	protected void destroySubscriber(String service, URL url, Date offlineDate){
		String address = createAddress(url);
		
		// 放入到回收池
		if(needUseGarbagePool()){
			if(!getGarbagePool().containKey(address)){
				if(devMode && !filterWithDevMode(url)){
					return ;
				}
				if(null != localAddress && localAddress.equals(address)){
					return ;
				}
				getGarbagePool().put(address, expiryOffline * 1000, url);
			}

			return ;
		}
		
		removeSubscriber(address, url);
	}

	protected void removeSubscriber(String address, URL url){

		PublisherGroup group = publisherGroups.get(address);
		
		if(needUseGarbagePool() && group != null){
			boolean health = false;
			try{
				// 先检测下是否健康
				health = group.getEventTransmission().checkHealth();
			}catch(Exception e){
				logger.error(new StringBuilder("check health failure before remove subscriber, reason:").append(e.getMessage()).append(", url:").append(url));
			}
			if(health){
				logger.info(new StringBuilder("recovery connection, subscriber won't be remove:").append(url));
				return ;
			}
			try {
				monitorMap.remove(address);
				eventForward.removeMonitor(group);
				if(logger.isTraceEnabled()){
					logger.trace(new StringBuilder("remove monitor for event forward:").append(address));
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(eventSubscribers.containsKey(address)){
			eventSubscribers.remove(address);
		}
		if(subscribEvents.containsKey(address)){
			subscribEvents.remove(address);
		}
		if(remoteSubscriberFactories.containsKey(address)){
			try{
				remoteSubscriberFactories.get(address).destroy();
				remoteSubscriberFactories.remove(address);
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
		
		if(publisherGroups.containsKey(address)){
			publisherGroups.remove(address);
		}
		if(publisherGroupFactories.containsKey(address)){
			try{
				publisherGroupFactories.get(address).destroy();
				publisherGroupFactories.remove(address);
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("destroy url:").append(url).append(" success"));
			}
		}
	}
	
	protected void destroyAllSubscribers(String service){
		if(serviceProviders == null || serviceProviders.size() == 0)
			return ;
		
		List<URL> urls = serviceProviders.get(service);
		for(URL url : urls){
			destroySubscriber(service, url, new Date());
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 这个主要用于 PublishEventCenter 设置setPublisherGroups，这个方法只接受 LocalPublisherGroup
	 */
	@Override
	public void publish(List<PublisherGroup> groups) {
		// 校验PublisherGroup类型
		/*for(PublisherGroup group : groups){
			if(group instanceof LocalPublisherGroup)
				continue;
			throw new IllegalArgumentException("It only support type of LocalPublisherGroup for method of setPublisherGroups");
		}*/
		
		if(null == localPublisherGroups){
			localPublisherGroups = new ArrayList<PublisherGroup>();
		}
		
		localPublisherGroups.addAll(groups);
	}

	@Override
	public List<PublisherGroup> getPublisherGroups() {
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>(publisherGroups.values());
		if(localPublisherGroups != null && localPublisherGroups.size() > 0){
			groups.addAll(localPublisherGroups);
		}
		
		return groups;
	}

	public void setExpiryOffline(int expiryOffline) {
		this.expiryOffline = expiryOffline;
	}

	@Override
	public int getExpiryOffline() {
		return expiryOffline;
	}
	
	@Override
	public void setForwardAndStorePolicy(EventForward eventForward,
			StoreAndForwardPolicy policy) {
		this.eventForward = eventForward;
		this.storeAndForwardPolicy = policy;
	}

	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	/**
	 * 将参数中的registryConfig拷贝到类中的registryConfig中
	 * @param config
	 */
	public void copy2RegistryConfig(RegistryConfig config){
		if(null == registryConfig){
			registryConfig = new RegistryConfig();
		}
		if(StringHelper.isNotEmpty(config.getAddress())){
			registryConfig.setAddress(config.getAddress());
		}
		if(StringHelper.isNotEmpty(config.getClient())){
			registryConfig.setClient(config.getClient());
		}
		if(StringHelper.isNotEmpty(config.getCluster())){
			registryConfig.setCluster(config.getCluster());
		}
		if(StringHelper.isNotEmpty(config.getFile())){
			registryConfig.setFile(config.getFile());
		}
		if(StringHelper.isNotEmpty(config.getGroup())){
			registryConfig.setGroup(config.getGroup());
		}
		if(StringHelper.isNotEmpty(config.getPassword())){
			registryConfig.setPassword(config.getPassword());
		}
		if(StringHelper.isNotEmpty(config.getProtocol())){
			registryConfig.setProtocol(config.getProtocol());
		}
		if(StringHelper.isNotEmpty(config.getServer())){
			registryConfig.setServer(config.getServer());
		}
		if(StringHelper.isNotEmpty(config.getTransporter())){
			registryConfig.setTransporter(config.getTransporter());
		}
		if(StringHelper.isNotEmpty(config.getUsername())){
			registryConfig.setUsername(config.getUsername());
		}
		if(StringHelper.isNotEmpty(config.getVersion())){
			registryConfig.setVersion(config.getVersion());
		}
		if(StringHelper.isNotEmpty(config.getId())){
			registryConfig.setId(config.getId());
		}
		if(null != config.getParameters()){
			registryConfig.setParameters(config.getParameters());
		}
		if(null != config.getPort()){
			registryConfig.setPort(config.getPort());
		}
		if(null != config.getSession()){
			registryConfig.setSession(config.getSession());
		}
		if(null != config.getTimeout()){
			registryConfig.setTimeout(config.getTimeout());
		}
	}

	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public String getLocalSubscriberId() {
		return localSubscriberId;
	}

	public void setLocalSubscriberId(String localSubscriberId) {
		this.localSubscriberId = localSubscriberId;
	}
}
