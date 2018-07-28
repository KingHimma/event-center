package eventcenter.remote.dubbo.publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.utils.StringHelper;
import eventcenter.remote.publisher.PublisherGroup;

/**
 * 创建{@link PublisherGroup}集合的工厂，如果这个工厂需要创建单个订阅者，只需要设置此工厂属性下的相关属性即可。
 * <pre>
 * 这里有这么几个原则，有关dubbo的配置优先以此工厂的属性为准，例如，publisherGroupFactories中的某个factory设置了registryConfig，并且工厂的registryConfig也设置了，
 * 那么优先使用工厂的registryConfig的配置，忽略掉集合中的设置，其他属性也是一样的原则
 * </pre>
 * @author JackyLIU
 *
 */
public class PublisherGroupsFactory extends PublisherGroupFactory {

	private List<PublisherGroupFactory> publisherGroupFactories;
	
	public List<PublisherGroup> createPublisherGroups(){
		/*if(getPublisherGroupFactories().size() == 0)
			// 至少需要一个发布事件组的工厂
			throw new IllegalArgumentException("at least need one publisherGroupFactory");*/
		if(getPublisherGroupFactories().size() == 0){
			PublisherGroupFactory factory = new PublisherGroupFactory();
			return Arrays.asList(assembly(factory).createPublisherGroup());
		}
		
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>();
		for(PublisherGroupFactory factory : getPublisherGroupFactories()){
			groups.add(assembly(factory).createPublisherGroup());
		}
		return groups;
	}
	
	/**
	 * 装配集合中的{@link #publisherGroupFactories}的工厂
	 * @param factory
	 * @return
	 */
	private PublisherGroupFactory assembly(PublisherGroupFactory factory){
		if(getApplicationConfig() != null)
			factory.setApplicationConfig(getApplicationConfig());
		if(getRegistryConfig() != null)
			factory.setRegistryConfig(getRegistryConfig());
		if(getProtocolConfig() != null)
			factory.setProtocolConfig(getProtocolConfig());
		if(StringHelper.isNotEmpty(getVersion()))
			factory.setVersion(getVersion());
		if(StringHelper.isNotEmpty(getUrl()))
			factory.setUrl(getUrl());
		if(StringHelper.isNotEmpty(getRemoteEvents()))
			factory.setRemoteEvents(getRemoteEvents());
		
		return factory;
	}

	public List<PublisherGroupFactory> getPublisherGroupFactories() {
		if(null == publisherGroupFactories)
			publisherGroupFactories = new ArrayList<PublisherGroupFactory>();
		return publisherGroupFactories;
	}

	public void setPublisherGroupFactories(
			List<PublisherGroupFactory> publisherGroupFactories) {
		this.publisherGroupFactories = publisherGroupFactories;
	}
}
