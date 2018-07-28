package eventcenter.remote.subscriber;

import eventcenter.api.EventFilter;
import eventcenter.remote.SubscriberGroup;

import java.util.Map;

/**
 * 当订阅器启动时，触发的filter操作
 *
 * @author liumingjian
 * @date 2017/4/11
 */
public interface SubscriberStartupFilter extends EventFilter {

    void onStartup(Map<String, SubscriberGroup> subscriberGroups);
}
