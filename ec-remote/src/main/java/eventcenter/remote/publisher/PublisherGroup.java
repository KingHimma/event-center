package eventcenter.remote.publisher;

import eventcenter.remote.EventTransmission;
import eventcenter.remote.utils.StringWildcard;

import java.util.*;

/**
 * 事件发布者分组
 * @author JackyLIU
 *
 */
public class PublisherGroup {

	/**
	 * 发布到远程事件的名称，多个名称使用','分割
	 */
	private String remoteEvents;
	
	private Set<String> remoteEventMap = new HashSet<String>();
	
	/**
	 * 包含了通配符的事件名称
	 */
	private List<String> containWildcardEvents = new ArrayList<String>();
	
	/**
	 * 分组名称
	 */
	private String groupName;
	

	private final EventTransmission eventTransmission;

	/**
	 * 远程的地址
	 */
	private String remoteUrl;

	public PublisherGroup(EventTransmission eventTransmission){
		this.eventTransmission = eventTransmission;
	}

	public EventTransmission getEventTransmission() {
		return eventTransmission;
	}

	public String getRemoteEvents() {
		return remoteEvents;
	}

	public void setRemoteEvents(String remoteEvents) {
		this.remoteEvents = remoteEvents;
		String[] remoteEventNames = remoteEvents.split(",");
		remoteEventMap = new HashSet<String>(remoteEventNames.length);
		for(String remoteEventName : remoteEventNames){
			if(remoteEventName.contains("*")){
				containWildcardEvents.add(remoteEventName);
				continue;
			}
			remoteEventMap.add(remoteEventName);
		}
	}
	
	/**
	 * 是否为远程事件
	 * @param eventName
	 * @return
	 */
	public boolean isRemoteEvent(String eventName){
		if(remoteEventMap.contains(eventName))
			return true;
		if(containWildcardEvents.size() == 0)
			return false;

		for(String wildcard : containWildcardEvents){
			if(StringWildcard.wildMatch(wildcard, eventName)){
				remoteEventMap.add(eventName);
				return true;
			}
		}
		return false;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	@Override
	public String toString() {
		return new StringBuilder("{groupName:").append(groupName).append(",remoteEvents:").append(remoteEvents).append(",remoteUrl:").append(remoteUrl).append("}").toString();
	}
}
