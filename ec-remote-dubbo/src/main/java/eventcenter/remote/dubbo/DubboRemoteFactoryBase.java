package eventcenter.remote.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import eventcenter.remote.utils.StringHelper;
import eventcenter.remote.utils.StringHelper;

/**
 * 事件中心服务提供者创建工厂
 * @author JackyLIU
 *
 */
public class DubboRemoteFactoryBase {

	private ApplicationConfig applicationConfig;
	
	private RegistryConfig registryConfig;
	
	private ProtocolConfig protocolConfig;
	
	/**
	 * Dubbo application.name设置，这个设置后，将会注入到{@link #getApplicationConfig()#applicationName}属性中
	 */
	private String applicationName;
	
	/**
	 * Dubbo registry.address设置，这个设置后，将会注入到{@link #getRegistryConfig()#registryAddress}属性中
	 */
	private String registryAddress;
	
	/**
	 * Dubbo registry.protocol设置，这个设置后，将会注入到{@link #getRegistryConfig()#registryProtocol}属性中
	 */
	private String registryProtocol;
	
	/**
	 * Dubbo registry.port设置，这个设置后，将会注入到{@link #getRegistryConfig()#registryPort}属性中
	 */
	private Integer registryPort; 
	
	/**
	 * Dubbo protocol.name设置，这个设置后，将会注入到{@link #getProtocolConfig()#protocolName}属性中
	 */
	private String protocolName;
	
	/**
	 * Dubbo protocol.name设置，这个设置后，将会注入到{@link #getProtocolConfig()#protocolPort}属性中
	 */
	private Integer protocolPort;
	
	/**
	 * Dubbo protocol.threads设置，这个设置后，将会注入到{@link #getProtocolConfig()#protocolThreads}属性中
	 */
	private Integer protocolThreads;
	
	/**
	 * dubbo服务或者引用的版本号
	 */
	private String version;

	public ApplicationConfig getApplicationConfig() {
		if(null == applicationConfig){
			if(StringHelper.isEmpty(applicationName))
				return applicationConfig;
			applicationConfig = new ApplicationConfig(applicationName);
		}
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public RegistryConfig getRegistryConfig() {
		if(null == registryConfig){
			if(StringHelper.isEmpty(registryAddress))
				return registryConfig;
			
			registryConfig = new RegistryConfig();
			registryConfig.setAddress(registryAddress);
			registryConfig.setRegister(false);
			if(StringHelper.isNotEmpty(registryProtocol))
				registryConfig.setProtocol(registryProtocol);
			if(null != registryPort)
				registryConfig.setPort(registryPort);
		}
		return registryConfig;
	}

	public void setRegistryConfig(RegistryConfig registryConfig) {
		this.registryConfig = registryConfig;
	}

	public ProtocolConfig getProtocolConfig() {
		if(null == protocolConfig){
			protocolConfig = new ProtocolConfig();
			if(StringHelper.isNotEmpty(protocolName))
				protocolConfig.setName(protocolName);
			if(null != protocolPort)
				protocolConfig.setPort(protocolPort);
			if(null != protocolThreads)
				protocolConfig.setThreads(protocolThreads);
			
		}
		return protocolConfig;
	}

	public void setProtocolConfig(ProtocolConfig protocolConfig) {
		this.protocolConfig = protocolConfig;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public Integer getProtocolPort() {
		return protocolPort;
	}

	public void setProtocolPort(Integer protocolPort) {
		this.protocolPort = protocolPort;
	}

	public Integer getProtocolThreads() {
		return protocolThreads;
	}

	public void setProtocolThreads(Integer protocolThreads) {
		this.protocolThreads = protocolThreads;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public String getRegistryProtocol() {
		return registryProtocol;
	}

	public void setRegistryProtocol(String registryProtocol) {
		this.registryProtocol = registryProtocol;
	}

	public Integer getRegistryPort() {
		return registryPort;
	}

	public void setRegistryPort(Integer registryPort) {
		this.registryPort = registryPort;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
