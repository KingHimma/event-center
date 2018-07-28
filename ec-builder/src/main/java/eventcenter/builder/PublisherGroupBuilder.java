package eventcenter.builder;

import eventcenter.remote.EventTransmission;
import eventcenter.remote.publisher.PublisherGroup;

/**
 * 构造{@link eventcenter.remote.publisher.PublisherGroup}
 *
 * @author liumingjian
 * @date 2017/9/1
 */
public class PublisherGroupBuilder {

    protected String remoteEvents;

    protected String groupName;

    protected EventTransmission eventTransmission;

    public PublisherGroupBuilder remoteEvents(String remoteEvents){
        this.remoteEvents = remoteEvents;
        return this;
    }

    public PublisherGroupBuilder groupName(String groupName){
        this.groupName = groupName;
        return this;
    }

    public PublisherGroupBuilder eventTransmission(EventTransmission eventTransmission){
        this.eventTransmission = eventTransmission;
        return this;
    }

    public PublisherGroup build(){
        PublisherGroup group = new PublisherGroup(this.eventTransmission);
        group.setGroupName(groupName);
        group.setRemoteEvents(remoteEvents);
        return group;
    }
}
