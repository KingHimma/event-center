package eventcenter.builder.dubbo;

import eventcenter.builder.SubscriberConfig;
import eventcenter.builder.SubscriberConfigBuilder;
import eventcenter.remote.SubscriberGroup;

/**
 *
 * @author liumingjian
 * @date 2017/9/6
 */
public class DubboSubscriberConfigBuilder extends SubscriberConfigBuilder {

    @Override
    protected SubscriberConfig createSubscriberConfig() {
        return new DubboSubscriberConfig();
    }

    DubboSubscriberConfig getDubboSubscriberConfig(){
        return (DubboSubscriberConfig)this.subscriberConfig;
    }

    public DubboSubscriberConfigBuilder eventSubscriberServiceConfig(EventSubscriberServiceConfig serviceConfig){
        getDubboSubscriberConfig().setServiceConfig(serviceConfig);
        return this;
    }

    public DubboSubscriberConfigBuilder addSubscriber(String remoteEvents){
        SubscriberGroup group = new SubscriberGroup();
        group.setRemoteEvents(remoteEvents);
        getDubboSubscriberConfig().getSubscriberGroups().add(group);
        return this;
    }


}
