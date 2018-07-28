package eventcenter.builder.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * 这个是个单例，构建PublishEventCenter之前，使用dubbo的方式，需要使用这个Context设置dubbo基本配置
 * Created by liumingjian on 2017/9/6.
 */
public class DubboConfigContext {

    private static DubboConfigContext self;

    private static Object lock = new Object();

    protected DubboConfig dubboConfig = new DubboConfig();

    protected String localSubscriberId;

    public static DubboConfigContext getInstance(){
        if(null == self){
            synchronized (lock){
                if(null == self){
                    self = new DubboConfigContext();
                }
            }
        }
        return self;
    }

    public DubboConfig getDubboConfig(){
        return this.dubboConfig;
    }

    public DubboConfigContext load(ApplicationContext context){
        if(null == dubboConfig.getApplicationConfig()) {
            dubboConfig.setApplicationConfig(getBean(context, ApplicationConfig.class));
        }
        if(null == dubboConfig.getProtocolConfig()) {
            dubboConfig.setProtocolConfig(getBean(context, ProtocolConfig.class));
        }
        if(null == dubboConfig.getRegistryConfig()) {
            dubboConfig.setRegistryConfig(getBean(context, RegistryConfig.class));
        }
        dubboConfig.setApplicationContext(context);
        return this;
    }

    private <T> T getBean(ApplicationContext context, Class<T> type){
        try {
            return context.getBean(type);
        }catch(NoSuchBeanDefinitionException e){
            return null;
        }
    }

    public DubboConfigContext applicationConfig(ApplicationConfig config){
        dubboConfig.setApplicationConfig(config);
        return this;
    }

    public DubboConfigContext applicationName(String name){
        if(null == dubboConfig.getApplicationConfig()){
            dubboConfig.setApplicationConfig(new ApplicationConfig());
        }
        dubboConfig.getApplicationConfig().setName(name);
        return this;
    }

    public DubboConfigContext applicationOwner(String owner){
        if(null == dubboConfig.getApplicationConfig()){
            dubboConfig.setApplicationConfig(new ApplicationConfig());
        }
        dubboConfig.getApplicationConfig().setOwner(owner);
        return this;
    }

    public DubboConfigContext protocolConfig(ProtocolConfig config){
        dubboConfig.setProtocolConfig(config);
        return this;
    }

    public DubboConfigContext protocolName(String name){
        if(null == dubboConfig.getProtocolConfig()){
            dubboConfig.setProtocolConfig(new ProtocolConfig());
        }
        dubboConfig.getProtocolConfig().setName(name);
        return this;
    }

    public DubboConfigContext protocolHost(String host){
        if(null == dubboConfig.getProtocolConfig()){
            dubboConfig.setProtocolConfig(new ProtocolConfig());
        }
        dubboConfig.getProtocolConfig().setHost(host);
        return this;
    }

    public DubboConfigContext protocolPort(Integer port){
        if(null == dubboConfig.getProtocolConfig()){
            dubboConfig.setProtocolConfig(new ProtocolConfig());
        }
        dubboConfig.getProtocolConfig().setPort(port);
        return this;
    }

    public DubboConfigContext registryConfig(RegistryConfig config){
        dubboConfig.setRegistryConfig(config);
        return this;
    }

    public DubboConfigContext registryProtocol(String protocol){
        if(null == dubboConfig.getRegistryConfig()){
            dubboConfig.setRegistryConfig(new RegistryConfig());
        }
        dubboConfig.getRegistryConfig().setProtocol(protocol);
        return this;
    }

    public DubboConfigContext registryAddress(String address){
        if(null == dubboConfig.getRegistryConfig()){
            dubboConfig.setRegistryConfig(new RegistryConfig());
        }
        dubboConfig.getRegistryConfig().setAddress(address);
        return this;
    }

    public DubboConfigContext registryUsername(String username){
        if(null == dubboConfig.getRegistryConfig()){
            dubboConfig.setRegistryConfig(new RegistryConfig());
        }
        dubboConfig.getRegistryConfig().setUsername(username);
        return this;
    }

    public DubboConfigContext registryPassword(String password){
        if(null == dubboConfig.getRegistryConfig()){
            dubboConfig.setRegistryConfig(new RegistryConfig());
        }
        dubboConfig.getRegistryConfig().setPassword(password);
        return this;
    }

    public DubboConfigContext groupName(String groupName){
        this.dubboConfig.setGroupName(groupName);
        return this;
    }

    public String getLocalSubscriberId() {
        return localSubscriberId;
    }

    void setLocalSubscriberId(String localSubscriberId) {
        this.localSubscriberId = localSubscriberId;
    }
}
