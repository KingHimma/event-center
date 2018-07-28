package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.remote.Target;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 默认的远程触发事件策略，使用for循环遍历每一个远程事件
 * @author JackyLIU
 *
 */
public class DefaultFireRemoteEventsPolicy extends AbstractFireRemoteEventsPolicy {

	protected final Logger logger = Logger.getLogger(this.getClass());

	public DefaultFireRemoteEventsPolicy(PublishEventCenter eventCenter) {
		super(eventCenter);
	}
	
	@Override
	public void fireRemoteEvents(List<PublisherGroup> groups, Target target,
			EventInfo eventInfo, Object result) {
		for (PublisherGroup group : groups) {
			try {
				asyncTransmission(group, target, eventInfo, result);
			} catch (Exception e) {
				logger.error(new StringBuilder("fire remote events error, group:").append(group.getGroupName()).append(",remote:").append(group.getRemoteUrl()).append(",event:").append(eventInfo));
			}
		}
	}

}
