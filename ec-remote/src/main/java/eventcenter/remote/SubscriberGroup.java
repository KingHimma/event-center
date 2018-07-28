package eventcenter.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 订阅分组
 * @author JackyLIU
 *
 */
public class SubscriberGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3686087463371987420L;

	/**
	 * 分组名称， 这个是必须要设置的
	 */
	private String name;
	
	/**
	 * 分组名称
	 */
	private String groupName;
	
	/**
	 * 订阅远程事件的名称，多个名称使用','分割
	 */
	private String remoteEvents;
	
	/**
	 * 订阅的配置中心地址
	 */
	private String address;
	
	/**
	 * 额外的属性
	 */
	private Map<String, Object> properties;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRemoteEvents() {
		return remoteEvents;
	}

	public void setRemoteEvents(String remoteEvents) {
		this.remoteEvents = remoteEvents;
	}

	public Map<String, Object> getProperties() {
		if(null == properties)
			properties = new HashMap<String, Object>();
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString(){
		return new StringBuilder("{groupName:").append(groupName).append(",remoteEvents:")
					.append(remoteEvents).append(",address:").append(address).append(",name:").append(name)
					.append(", properties:").append(properties).append("}").toString();
	}
}
