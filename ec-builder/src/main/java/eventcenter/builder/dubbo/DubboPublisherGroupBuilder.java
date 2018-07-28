package eventcenter.builder.dubbo;

import eventcenter.builder.PublisherGroupBuilder;
import eventcenter.remote.publisher.PublisherGroup;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class DubboPublisherGroupBuilder extends PublisherGroupBuilder {

    protected EventTransmissionReferenceConfig refConfig;

    protected volatile boolean isLoad = false;

    public DubboPublisherGroupBuilder eventTransmission(EventTransmissionReferenceConfig refConfig) {
        this.refConfig = refConfig;
        return this;
    }

    public EventTransmissionReferenceConfig getRefConfig(){
        return this.refConfig;
    }

    DubboPublisherGroupBuilder load(DubboConfig dubboConfig){
        if(null == this.refConfig){
            throw new IllegalArgumentException("please set eventTransmission");
        }
        if(dubboConfig.getApplicationConfig() != null){
            this.refConfig.setApplication(dubboConfig.getApplicationConfig());
        }
        if(dubboConfig.getRegistryConfig() != null){
            this.refConfig.setRegistry(dubboConfig.getRegistryConfig());
        }
        this.refConfig.setCheck(false);
        if(this.refConfig.getId() == null){
            this.refConfig.setId(createRefId());
        }
        if(this.groupName == null) {
            groupName(dubboConfig.getGroupName());
        }
        if(this.refConfig.getGroup() == null){
            this.refConfig.setGroup(this.groupName);
        }
        this.refConfig.load();
        isLoad = true;
        return this;
    }

    String createRefId(){
        return new StringBuilder("eventTransmission").append(System.currentTimeMillis()).toString();
    }

    @Override
    public PublisherGroup build() {
        if(!isLoad)
            throw new IllegalArgumentException("please load first");
        checkAndRefillRefConfig();
        this.eventTransmission = this.refConfig.get();
        return super.build();
    }

    /**
     * 在spring的配置环境下，this.refConfig第一次load是在xml构建factory bean时调用，如果conf.dubbo配置下未设置application等dubbo的
     * 基本信息时，这里的this.refConfig中的application和registry等一些信息还是为空；所以需要在build的方法中，再次检查下这些信息是否设置进去，
     * 如果未设置，则还需从{@link DubboConfigContext}中获取，因为build方法是在spring初始化所有相关的factory bean之后开始，所以，可以获取到
     * dubbo自有的application等信息
     */
    void checkAndRefillRefConfig(){
        DubboConfig dubboConfig = DubboConfigContext.getInstance().getDubboConfig();
        if(this.refConfig.getApplication() == null){
            this.refConfig.setApplication(dubboConfig.applicationConfig);
        }
        if(this.refConfig.getRegistry() == null){
            this.refConfig.setRegistry(dubboConfig.getRegistryConfig());
        }
    }
}
