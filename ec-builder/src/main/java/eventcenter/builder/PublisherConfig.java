package eventcenter.builder;

import eventcenter.remote.EventPublisher;
import eventcenter.remote.publisher.LocalPublisherGroup;
import eventcenter.remote.publisher.PublishEventCenter;
import eventcenter.remote.publisher.PublisherGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 事件发布者配置
 * Created by liumingjian on 2017/9/1.
 */
public class PublisherConfig implements Serializable{

    private static final long serialVersionUID = 2520698005690310859L;

    protected PublisherConfig(){}

    protected List<PublisherGroupBuilder> groupBuilders;

    protected LocalPublisherGroup localPublisherGroup;

    protected EventPublisher eventPublisher;

    /**
     * 是否使用异步触发事件
     */
    protected Boolean asyncFireRemote;

    public List<PublisherGroupBuilder> getGroupBuilders() {
        if(null == groupBuilders){
            groupBuilders = new ArrayList<PublisherGroupBuilder>();
        }
        return groupBuilders;
    }

    public void setGroupBuilders(List<PublisherGroupBuilder> groupBuilders) {
        this.groupBuilders = groupBuilders;
    }

    public LocalPublisherGroup getLocalPublisherGroup() {
        return localPublisherGroup;
    }

    public void setLocalPublisherGroup(LocalPublisherGroup localPublisherGroup) {
        this.localPublisherGroup = localPublisherGroup;
    }

    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Boolean getAsyncFireRemote() {
        return asyncFireRemote;
    }

    public void setAsyncFireRemote(Boolean asyncFireRemote) {
        this.asyncFireRemote = asyncFireRemote;
    }

    public PublishEventCenter load(PublishEventCenter eventCenter){
        if(null != asyncFireRemote){
            eventCenter.setAsyncFireRemote(asyncFireRemote);
        }
        if(null != localPublisherGroup){
            eventCenter.getPublisherGroups().add(localPublisherGroup);
        }
        if(null != getEventPublisher()) {
            eventCenter.setEventPublisher(getEventPublisher());
        }
        List<PublisherGroupBuilder> groupBuilders = getGroupBuilders();
        if(groupBuilders.size() > 0){
            List<PublisherGroup> publisherGroups = new ArrayList<PublisherGroup>(groupBuilders.size());
            for(PublisherGroupBuilder publisherGroupBuilder : getGroupBuilders()){
                publisherGroups.add(publisherGroupBuilder.build());
            }
            eventCenter.getEventPublisher().publish(publisherGroups);
        }
        return eventCenter;
    }
}
