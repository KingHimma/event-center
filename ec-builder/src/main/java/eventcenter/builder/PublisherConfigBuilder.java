package eventcenter.builder;

import eventcenter.remote.EventTransmission;
import eventcenter.remote.publisher.LocalPublisherGroup;

/**
 * 事件发布器构建器
 * Created by liumingjian on 2017/9/1.
 */
public class PublisherConfigBuilder {

    protected PublisherConfig publisherConfig = createPublisherConfig();

    protected PublisherConfig createPublisherConfig(){
        return new PublisherConfig();
    }

    /**
     * 添加发布-订阅的发布者的分组，当使用非动态订阅事件的方式时，需要直接添加publisherGroup进来。
     * 也就是在发布者这边配置订阅者的{@link EventTransmission}实例，由发布者来路由到相关的订阅者
     * @param group
     * @return
     */
    public PublisherConfigBuilder addPublisherGroup(PublisherGroupBuilder group){
        publisherConfig.getGroupBuilders().add(group);
        return this;
    }

    /**
     * 某些事件如果是发送到远程订阅者时，如果本地节点也需要订阅此事件，应该需要使用{@link LocalPublisherGroup}配置路由。
     * 只需要设置本地需要消费的事件，多个事件请使用','逗号分隔，可以使用'*'通配符匹配多个事件
     * @param eventNames
     * @return
     */
    public PublisherConfigBuilder addLocalPublisherGroup(String eventNames){
        publisherConfig.setLocalPublisherGroup(new LocalPublisherGroup());
        publisherConfig.getLocalPublisherGroup().setRemoteEvents(eventNames);
        return this;
    }

    /**
     * 是否使用异步触发事件
     * @param asyncFireRemote
     * @return
     */
    public PublisherConfigBuilder asyncFireRemote(Boolean asyncFireRemote){
        publisherConfig.setAsyncFireRemote(asyncFireRemote);
        return this;
    }

    public PublisherConfig build(){
        return publisherConfig;
    }
}
