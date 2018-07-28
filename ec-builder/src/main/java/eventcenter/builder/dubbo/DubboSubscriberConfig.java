package eventcenter.builder.dubbo;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.SubscriberConfig;
import eventcenter.remote.EventSubscriber;
import eventcenter.remote.SubscriberGroup;

/**
 *
 * @author liumingjian
 * @date 2017/9/6
 */
public class DubboSubscriberConfig extends SubscriberConfig {

    private static final long serialVersionUID = -3578154819486724152L;
    protected EventSubscriberServiceConfig serviceConfig;

    protected eventcenter.remote.subscriber.EventSubscriber subscriber = new eventcenter.remote.subscriber.EventSubscriber();

    @Override
    public EventSubscriber load(DefaultEventCenter eventCenter) {
        if(null == serviceConfig){
            throw new IllegalArgumentException("please set serviceConfig");
        }
        DubboConfig dubboConfig = DubboConfigContext.getInstance().getDubboConfig();
        if(null != dubboConfig.getApplicationConfig()){
            serviceConfig.setApplication(dubboConfig.getApplicationConfig());
        }
        if(null != dubboConfig.getProtocolConfig()){
            serviceConfig.setProtocol(dubboConfig.getProtocolConfig());
        }
        if(null != dubboConfig.getRegistryConfig()){
            serviceConfig.setRegistry(dubboConfig.getRegistryConfig());
        }

        String groupName = serviceConfig.getGroup() != null?serviceConfig.getGroup():DubboConfigContext.getInstance().getDubboConfig().getGroupName();
        boolean loadSubscriber = false;
        if(null != groupName && getSubscriberGroups().size() > 0) {
            for (SubscriberGroup subscriberGroup : getSubscriberGroups()) {
                subscriberGroup.setGroupName(groupName);
            }
            loadSubscriber = true;
            subscriber.setSubscriberGroups(getSubscriberGroups());
        }
        if(null != groupName){
            serviceConfig.setGroup(groupName);
        }
        subscriber.setEventCenter(eventCenter);
        serviceConfig.load(subscriber, loadSubscriber);
        DubboConfigContext.getInstance().setLocalSubscriberId(subscriber.getId());
        return subscriber;
    }

    public EventSubscriberServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(EventSubscriberServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public eventcenter.remote.subscriber.EventSubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(eventcenter.remote.subscriber.EventSubscriber subscriber) {
        this.subscriber = subscriber;
    }
}
