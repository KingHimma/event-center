package eventcenter.builder;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.remote.EventSubscriber;
import eventcenter.remote.SubscriberGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 事件订阅器配置
 *
 * @author liumingjian
 * @date 2017/9/1
 */
public class SubscriberConfig implements Serializable {
    private static final long serialVersionUID = 8215025345650834595L;

    protected List<SubscriberGroup> subscriberGroups = new ArrayList<SubscriberGroup>();

    protected SubscriberConfig(){}

    public EventSubscriber load(DefaultEventCenter eventCenter){
        return new eventcenter.remote.subscriber.EventSubscriber(eventCenter);
    }

    public List<SubscriberGroup> getSubscriberGroups() {
        if(null == subscriberGroups){
            subscriberGroups = new ArrayList<SubscriberGroup>();
        }
        return subscriberGroups;
    }

    public void setSubscriberGroups(List<SubscriberGroup> subscriberGroups) {
        this.subscriberGroups = subscriberGroups;
    }
}
