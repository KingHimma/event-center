package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.RegistryService;
import eventcenter.remote.dubbo.DubboRemoteFactoryBase;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Dubbo注册服务工厂，如果spring或者配置文件中没有配置，那么这个工厂将会自动加载RegistryService
 * @author JackyLIU
 *
 */
public class RegistryServiceFactory extends DubboRemoteFactoryBase {

	private ReferenceConfig<RegistryService> referenceConfig;
	
	public RegistryService createRegistryService(){
		return getReferenceConfig().get();
	}

	public static RegistryService buildDefault(ApplicationContext applicationContext){
		try {
			return applicationContext.getBean(RegistryService.class);
		}catch(NoSuchBeanDefinitionException e){

		}

		ReferenceConfig<RegistryService> config = new ReferenceConfig<RegistryService>();
		try {
			ApplicationConfig applicationConfig = applicationContext.getBean(ApplicationConfig.class);
			config.setApplication(applicationConfig);
		}catch(NoSuchBeanDefinitionException e){
		}
		try{
			RegistryConfig registryConfig = applicationContext.getBean(RegistryConfig.class);
			config.setRegistry(registryConfig);
		}catch (NoSuchBeanDefinitionException e){
		}
		config.setInterface(RegistryService.class);
		config.setId("registryService" + System.currentTimeMillis());
		return config.get();
	}
	
	public static RegistryServiceFactory buildWith(URL url, String applicationName){
		RegistryServiceFactory factory = buildWith(url, applicationName, null);
		factory.getReferenceConfig().setUrl(new StringBuilder("dubbo://").append(url.getHost()).append(":").append(url.getPort()).toString());
		return factory;
	}
	
	public static RegistryServiceFactory buildWith(URL url, String applicationName, RegistryConfig registryConfig){
		RegistryServiceFactory factory = new RegistryServiceFactory();
		factory.setApplicationName(applicationName);
		factory.setVersion(url.getParameter("version"));
		factory.setRegistryConfig(registryConfig);
		factory.getReferenceConfig().setGroup(url.getParameter("group"));
		return factory;
	}

	public static RegistryServiceFactory buildWith(ReferenceConfig<RegistryService> registryServiceConfig){
		RegistryServiceFactory factory = new RegistryServiceFactory();
		factory.setReferenceConfig(registryServiceConfig);
		return factory;
	}

	public ReferenceConfig<RegistryService> getReferenceConfig() {
		if(null == referenceConfig)
			referenceConfig = new ReferenceConfig<RegistryService>();
		return referenceConfig;
	}

	public void setReferenceConfig(ReferenceConfig<RegistryService> referenceConfig) {
		this.referenceConfig = referenceConfig;
	}
}
