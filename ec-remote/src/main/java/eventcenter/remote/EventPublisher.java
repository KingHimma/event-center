package eventcenter.remote;

import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.publisher.PublisherGroup;

import java.util.List;

/**
 * 事件发布端
 * @author JackyLIU
 *
 */
public interface EventPublisher {
	
	/**
	 * 启动
	 */
	void startup() throws Exception;
	
	/**
	 * 关闭
	 */
	void shutdown() throws Exception;
	
	/**
	 * 发布远程事件
	 * @param groups
	 */
	void publish(List<PublisherGroup> groups);
	
	List<PublisherGroup> getPublisherGroups();
}
