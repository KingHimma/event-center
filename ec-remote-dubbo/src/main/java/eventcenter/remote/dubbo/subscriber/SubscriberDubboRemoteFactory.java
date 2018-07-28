package eventcenter.remote.dubbo.subscriber;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import eventcenter.api.EventCenter;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.dubbo.DubboRemoteFactoryBase;
import eventcenter.remote.subscriber.EventSubscriber;
import eventcenter.remote.subscriber.EventSubscriber;

/**
 * 构建SubscriberEventCenter工厂
 * @author JackyLIU
 *
 */
public class SubscriberDubboRemoteFactory extends DubboRemoteFactoryBase {
	
	private ServiceConfig<EventTransmission> serviceConfig;
	
	/**
	 * {@link EventTransmission#checkHealth()}的dubbo配置
	 */
	private MethodConfig checkHealthMethodConfig;
	
	/**
	 * {@link EventTransmission#asyncTransmission(eventcenter.remote.Target, eventcenter.api.EventInfo, Object)}的dubbo配置
	 */
	private MethodConfig asyncTransmissionMethodConfig;
	
	/**
	 * 构建{@link EventTransmission}事件接收端
	 * @return
	 */
	public EventTransmission createEventTransmission(EventCenter localEventCenter){
		EventSubscriber es = new EventSubscriber(localEventCenter);
		
		if(null == serviceConfig){
			serviceConfig = new ServiceConfig<EventTransmission>();
		}
		
		if(null != getApplicationConfig()){
			serviceConfig.setApplication(getApplicationConfig());
		}
		if(null != getProtocolConfig()){
			serviceConfig.setProtocol(getProtocolConfig());
		}
		if(null != getRegistryConfig()){
			serviceConfig.setRegistry(getRegistryConfig());
		}
		if(null != getVersion())
			serviceConfig.setVersion(getVersion());
		
		serviceConfig.setRef(es);
		
		// 配置方法
		List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>();
		MethodConfig checkHealthMethod = getCheckHealthMethodConfig();
		checkHealthMethod.setName("checkHealth");
		if(null == checkHealthMethod.getTimeout()){
			checkHealthMethod.setTimeout(1000);
		}
		methodConfigs.add(checkHealthMethod);
		MethodConfig asyncTransmissionMethodConfig = getAsyncTransmissionMethodConfig();
		asyncTransmissionMethodConfig.setName("asyncTransmission");
		asyncTransmissionMethodConfig.setAsync(true);
		asyncTransmissionMethodConfig.setReturn(false);
		methodConfigs.add(asyncTransmissionMethodConfig);
		
		serviceConfig.setMethods(methodConfigs);
		serviceConfig.setInterface(EventTransmission.class);
		serviceConfig.export();
		return es;
	}

	public ServiceConfig<EventTransmission> getServiceConfig() {
		return serviceConfig;
	}

	public void setServiceConfig(ServiceConfig<EventTransmission> serviceConfig) {
		this.serviceConfig = serviceConfig;
	}

	public MethodConfig getCheckHealthMethodConfig() {
		if(null == checkHealthMethodConfig){
			checkHealthMethodConfig = new MethodConfig();
		}
		return checkHealthMethodConfig;
	}

	public void setCheckHealthMethodConfig(MethodConfig checkHealthMethodConfig) {
		this.checkHealthMethodConfig = checkHealthMethodConfig;
	}

	public MethodConfig getAsyncTransmissionMethodConfig() {
		if(null == asyncTransmissionMethodConfig)
			asyncTransmissionMethodConfig = new MethodConfig();
		return asyncTransmissionMethodConfig;
	}

	public void setAsyncTransmissionMethodConfig(
			MethodConfig asyncTransmissionMethodConfig) {
		this.asyncTransmissionMethodConfig = asyncTransmissionMethodConfig;
	}
}
