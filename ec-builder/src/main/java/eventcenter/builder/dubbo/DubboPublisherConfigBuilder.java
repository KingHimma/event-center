package eventcenter.builder.dubbo;

import eventcenter.builder.PublisherConfig;
import eventcenter.builder.PublisherConfigBuilder;
import eventcenter.builder.PublisherGroupBuilder;
import org.springframework.context.ApplicationContext;

/**
 * 构建基于dubbo的publisher，需要依赖{@link ApplicationContext}
 * Created by liumingjian on 2017/9/5.
 */
public class DubboPublisherConfigBuilder extends PublisherConfigBuilder {

    @Override
    protected PublisherConfig createPublisherConfig() {
        return new DubboPublisherConfig();
    }

    DubboPublisherConfig getPublisherConfig(){
        return (DubboPublisherConfig)this.publisherConfig;
    }

    @Override
    public DubboPublisherConfigBuilder addPublisherGroup(PublisherGroupBuilder group) {
        if(!(group instanceof DubboPublisherGroupBuilder)) {
            super.addPublisherGroup(group);
            return this;
        }
        DubboPublisherGroupBuilder builder = (DubboPublisherGroupBuilder)group;
        if(null == builder.getRefConfig()){
            throw new IllegalArgumentException("please set refConfig");
        }
        super.addPublisherGroup(group);
        return this;
    }

    public DubboPublisherConfigBuilder subscriberAutowired(Boolean autowired){
        getPublisherConfig().setSubscriberAutowired(autowired);
        return this;
    }

    public DubboPublisherConfigBuilder groupName(String groupName){
        getPublisherConfig().setGroupName(groupName);
        return this;
    }

    public DubboPublisherConfigBuilder copySendUnderSameVersion(Boolean value){
        getPublisherConfig().setCopySendUnderSameVersion(value);
        return this;
    }

    public DubboPublisherConfigBuilder devMode(Boolean devMode){
        getPublisherConfig().setDevMode(devMode);
        return this;
    }

    @Override
    public DubboPublisherConfigBuilder addLocalPublisherGroup(String eventNames) {
        return (DubboPublisherConfigBuilder)super.addLocalPublisherGroup(eventNames);
    }

    public DubboPublisherConfigBuilder expiryOffline(Long expiryOffline){
        getPublisherConfig().setExpiryOffline(expiryOffline);
        return this;
    }

    @Override
    public DubboPublisherConfig build(){
        for(PublisherGroupBuilder publisherGroupBuilder : publisherConfig.getGroupBuilders()){
            if(!(publisherGroupBuilder instanceof DubboPublisherGroupBuilder))
                continue;
            ((DubboPublisherGroupBuilder)publisherGroupBuilder).load(DubboConfigContext.getInstance().getDubboConfig());
        }
        return getPublisherConfig();
    }
}
