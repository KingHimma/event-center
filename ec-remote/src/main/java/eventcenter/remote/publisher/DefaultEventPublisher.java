package eventcenter.remote.publisher;

import java.util.ArrayList;
import java.util.List;

import eventcenter.remote.EventPublisher;

/**
 * 默认的发布端
 * @author JackyLIU
 *
 */
public class DefaultEventPublisher implements EventPublisher {

	protected List<PublisherGroup> publisherGroups;	
	
	@Override
	public void publish(List<PublisherGroup> groups) {
		getPublisherGroups().addAll(groups);
	}

	@Override
	public List<PublisherGroup> getPublisherGroups() {
		if(null == publisherGroups)
			publisherGroups = new ArrayList<PublisherGroup>();
		return publisherGroups;
	}

	@Override
	public void startup() {
		// 默认的不需要启动
	}

	@Override
	public void shutdown() {
		
	}

}
