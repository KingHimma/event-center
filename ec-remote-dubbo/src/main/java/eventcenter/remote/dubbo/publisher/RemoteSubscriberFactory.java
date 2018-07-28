package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.config.ApplicationConfig;
import eventcenter.remote.EventSubscriber;
import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import eventcenter.remote.dubbo.DubboRemoteFactoryBase;
import eventcenter.remote.utils.StringHelper;

/**
 * 创建{@link EventSubscriber}调用代理的工厂
 * @author JackyLIU
 *
 */
public class RemoteSubscriberFactory extends DubboRemoteFactoryBase {

	private ReferenceConfig<EventSubscriber> referenceConfig;
	
	/**
	 * 连接dubbo远程服务端的地址，如果配置了registerConfig可以不用设置这个
	 */
	private String url;
	
	private EventSubscriber eventSubscriber;
	
	private ReferenceConfig<EventSubscriber> instanceRefConfig;

	private ApplicationConfig applicationConfig;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public EventSubscriber createEventSubscriber(){
		if(null != eventSubscriber)
			return eventSubscriber;
		
		instanceRefConfig = getReferenceConfig();
		
		if(StringHelper.isNotEmpty(getVersion()))
			instanceRefConfig.setVersion(getVersion());
		
		if(null != getApplicationConfig())
			instanceRefConfig.setApplication(getApplicationConfig());
		if(null != getRegistryConfig())
			instanceRefConfig.setRegistry(getRegistryConfig());
		if(StringHelper.isNotEmpty(url))
			instanceRefConfig.setUrl(url);
		instanceRefConfig.setInterface(EventSubscriber.class);
		instanceRefConfig.setCheck(false);

		EventSubscriber et = instanceRefConfig.get();

		return et;
	}
	
	public synchronized boolean destroy(){
		if(null == eventSubscriber)
			return false;
		
		try{
			instanceRefConfig.destroy();
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		eventSubscriber = null;
		return true;
	}
	
	public ReferenceConfig<EventSubscriber> getReferenceConfig() {
		if(null == referenceConfig)
			referenceConfig = new ReferenceConfig<EventSubscriber>();
		return referenceConfig;
	}

	public void setReferenceConfig(
			ReferenceConfig<EventSubscriber> referenceConfig) {
		this.referenceConfig = referenceConfig;
	}
	
	public static RemoteSubscriberFactory buildWith(URL url, String applicationName){
		RemoteSubscriberFactory factory = buildWith(url, applicationName, null);
		factory.getReferenceConfig().setUrl(new StringBuilder("dubbo://").append(url.getHost()).append(":").append(url.getPort()).toString());
		return factory;
	}
	
	public static RemoteSubscriberFactory buildWith(URL url, String applicationName, RegistryConfig registryConfig){
		return buildWith(url, applicationName, registryConfig, null);
	}

	public static RemoteSubscriberFactory buildWith(URL url, String applicationName, RegistryConfig registryConfig, ApplicationConfig applicationConfig){
		RemoteSubscriberFactory factory = new RemoteSubscriberFactory();
		factory.setApplicationName(applicationName);
		factory.setVersion(url.getParameter("version"));
		if(null != registryConfig){
			registryConfig.setRegister(false);
		}
		factory.setApplicationConfig(applicationConfig);
		factory.setRegistryConfig(registryConfig);
		factory.getReferenceConfig().setGroup(url.getParameter("group"));
		return factory;
	}
}
