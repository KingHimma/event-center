package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.config.ApplicationConfig;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.publisher.PublisherGroup;
import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import eventcenter.remote.dubbo.DubboRemoteFactoryBase;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.utils.StringHelper;

/**
 * 构建{@link PublisherGroup}的工厂
 * @author JackyLIU
 *
 */
public class PublisherGroupFactory extends DubboRemoteFactoryBase {

	private ReferenceConfig<EventTransmission> referenceConfig;
	
	/**
	 * 连接dubbo远程服务端的地址，如果配置了registerConfig可以不用设置这个
	 */
	private String url;
	
	/**
	 * 需要发送到远程事件的事件名称，多个事件名称使用','符号分割
	 */
	private String remoteEvents;
	
	private PublisherGroup publisherGroup;
	
	private ReferenceConfig<EventTransmission> instanceRefConfig;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 创建的PublisherGroup为单例模式，如果需要重新创建，则应该先
	 * @return
	 */
	public PublisherGroup createPublisherGroup(){
		if(null == publisherGroup) {
			instanceRefConfig = getReferenceConfig();

			if(StringHelper.isNotEmpty(getVersion()))
				instanceRefConfig.setVersion(getVersion());

			if(null != getApplicationConfig())
				instanceRefConfig.setApplication(getApplicationConfig());
			if(null != getRegistryConfig())
				instanceRefConfig.setRegistry(getRegistryConfig());
			if(StringHelper.isNotEmpty(url))
				instanceRefConfig.setUrl(url);
			instanceRefConfig.setInterface(EventTransmission.class);
			instanceRefConfig.setCheck(false);

			EventTransmission et = instanceRefConfig.get();
			publisherGroup = new PublisherGroup(et);
		}
		publisherGroup.setRemoteEvents(remoteEvents);
		return publisherGroup;
	}
	
	public synchronized boolean destroy(){
		if(null == publisherGroup)
			return false;
		
		try{
			instanceRefConfig.destroy();
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		publisherGroup = null;
		return true;
	}

	public ReferenceConfig<EventTransmission> getReferenceConfig() {
		if(null == referenceConfig)
			referenceConfig = new ReferenceConfig<EventTransmission>();
		return referenceConfig;
	}

	public void setReferenceConfig(
			ReferenceConfig<EventTransmission> referenceConfig) {
		this.referenceConfig = referenceConfig;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemoteEvents() {
		return remoteEvents;
	}

	public void setRemoteEvents(String remoteEvents) {
		this.remoteEvents = remoteEvents;
	}
	
	/**
	 * 使用 {@link URL}创建工厂，这里将直接使用url提供的IP地址作为远程访问，如果需要使用到zk或者registry，请调用 {@link #buildWith(URL, RegistryConfig)}
	 * @param url
	 * @return
	 */
	public static PublisherGroupFactory buildWith(URL url, String remoteEvents, String applicationName){
		PublisherGroupFactory factory = buildWith(url, remoteEvents, applicationName, null);
		factory.getReferenceConfig().setUrl(new StringBuilder("dubbo://").append(url.getHost()).append(":").append(url.getPort()).toString());
		return factory;
	}

	public static PublisherGroupFactory buildWith(URL url, String remoteEvents, ApplicationConfig application){
		PublisherGroupFactory factory = buildWith(url, remoteEvents, application, null);
		factory.getReferenceConfig().setUrl(new StringBuilder("dubbo://").append(url.getHost()).append(":").append(url.getPort()).toString());
		return factory;
	}

	public static PublisherGroupFactory buildWith(URL url, String remoteEvents, ApplicationConfig application, RegistryConfig registry){
		PublisherGroupFactory factory = new PublisherGroupFactory();
		factory.setApplicationConfig(application);
		factory.setVersion(url.getParameter("version"));
		factory.setRemoteEvents(remoteEvents);
		factory.setRegistryConfig(registry);
		factory.getReferenceConfig().setGroup(url.getParameter("group"));
		return factory;
	}
	
	public static PublisherGroupFactory buildWith(URL url, String remoteEvents, String applicationName, RegistryConfig registryConfig){
		PublisherGroupFactory factory = new PublisherGroupFactory();
		factory.setApplicationName(applicationName);
		factory.setVersion(url.getParameter("version"));
		factory.setRemoteEvents(remoteEvents);
		factory.setRegistryConfig(registryConfig);
		factory.getReferenceConfig().setGroup(url.getParameter("group"));
		return factory;
	}
}
