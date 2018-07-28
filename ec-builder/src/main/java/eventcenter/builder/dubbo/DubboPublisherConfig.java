package eventcenter.builder.dubbo;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.registry.RegistryService;
import eventcenter.builder.PublisherConfig;
import eventcenter.remote.dubbo.publisher.DubboRegistryEventPublisher;
import eventcenter.remote.publisher.PublishEventCenter;
import eventcenter.remote.utils.StringHelper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class DubboPublisherConfig extends PublisherConfig {
    private static final long serialVersionUID = -4294792096771074963L;

    /**
     * 是否开启自动发现订阅器的机制，使用这台机器可以不用配置PublisherGroupBuilder，但是需要设置groupName等等相关属性
     */
    private Boolean subscriberAutowired;

    private String groupName;

    private Boolean copySendUnderSameVersion;

    private Boolean devMode;

    private Long expiryOffline;

    public void setGroupName(String groupName){
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public Boolean getCopySendUnderSameVersion() {
        return copySendUnderSameVersion;
    }

    public void setCopySendUnderSameVersion(Boolean copySendUnderSameVersion) {
        this.copySendUnderSameVersion = copySendUnderSameVersion;
    }

    public Boolean getDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    public Long getExpiryOffline() {
        return expiryOffline;
    }

    public void setExpiryOffline(Long expiryOffline) {
        this.expiryOffline = expiryOffline;
    }

    public Boolean getSubscriberAutowired() {
        return subscriberAutowired;
    }

    public void setSubscriberAutowired(Boolean subscriberAutowired) {
        this.subscriberAutowired = subscriberAutowired;
    }

    @Override
    public PublishEventCenter load(PublishEventCenter eventCenter) {
        if(null == getSubscriberAutowired() || !getSubscriberAutowired()){
            return super.load(eventCenter);
        }
        DubboConfig dubboConfig = DubboConfigContext.getInstance().getDubboConfig();
        if(null == dubboConfig.getRegistryConfig()){
            throw new IllegalArgumentException("please set registryConfig when subscriberAutowired is open");
        }
        if(StringHelper.isEmpty(dubboConfig.getRegistryConfig().getAddress())){
            throw new IllegalArgumentException("please set address of registryConfig");
        }
        DubboRegistryEventPublisher eventPublisher = new DubboRegistryEventPublisher();
        eventPublisher.setDubboGroup(null != groupName ? groupName : dubboConfig.getGroupName());
        if(null != copySendUnderSameVersion) {
            eventPublisher.setCopySendUnderSameVersion(copySendUnderSameVersion);
        }
        if(null != devMode) {
            eventPublisher.setDevMode(devMode);
        }
        if(null != expiryOffline) {
            eventPublisher.setExpiryOffline(expiryOffline.intValue());
        }
        if(null != dubboConfig.getApplicationContext()){
            eventPublisher.setApplicationContext(dubboConfig.getApplicationContext());
        }
        eventPublisher.copy2RegistryConfig(dubboConfig.getRegistryConfig()); // TODO 这里获取address可能有问题
        if(null != DubboConfigContext.getInstance().getLocalSubscriberId()){
            eventPublisher.setLocalSubscriberId(DubboConfigContext.getInstance().getLocalSubscriberId());
        }
        eventPublisher.setApplicationConfig(dubboConfig.getApplicationConfig());
        // 构建registryConfig
        initRegistryService(eventPublisher, dubboConfig);
        // eventPublisher.startup();
        this.eventPublisher = eventPublisher;
        return super.load(eventCenter);
    }

    private void initRegistryService(DubboRegistryEventPublisher eventPublisher, DubboConfig dubboConfig){
        if(dubboConfig.getApplicationContext() != null){
            try {
                dubboConfig.getApplicationContext().getBean(RegistryService.class);
                return ;
            }catch(NoSuchBeanDefinitionException e){
                // TODO 打日志
            }
        }

        ReferenceConfig<RegistryService> config = new ReferenceConfig<RegistryService>();
        config.setApplication(dubboConfig.getApplicationConfig());
        config.setRegistry(dubboConfig.getRegistryConfig());
        config.setInterface(RegistryService.class);
        config.setId("registryService" + System.currentTimeMillis());
        eventPublisher.setRegistryService(config.get());
    }
}
