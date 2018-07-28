package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import eventcenter.remote.EventSubscriber;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.SubscriberGroup;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.publisher.PublisherGroup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试dubbo的RegistryService的订阅服务
 * @author JackyLIU
 *
 */
public class TestPublisherNotifyChange {

	private DubboRegistryEventPublisher publisher;
	
	private RegistryService registryService;
	
	private NotifyListener listener;
	
	public static final String group = "test";
	
	public static final String version = "test";
	
	@Before
	public void setUp(){
		registryService = Mockito.mock(RegistryService.class);
		publisher = Mockito.spy(new DubboRegistryEventPublisher());
		System.setProperty(DubboRegistryEventPublisher.APPLICATION_NAME, "publisherTest");
		System.setProperty(DubboRegistryEventPublisher.REGISTRY_ADDRESS, "localhost:2181");
		publisher.setDubboGroup(group);		
		publisher.setRegistryService(registryService);
		
		listener = new NotifyListener(){
			@Override
			public void notify(List<URL> urls) {
				publisher._notify(urls);
			}
		};
		Mockito.when(publisher.createNotifyListener()).thenReturn(listener);
		
		publisher.startup();
	}
	
	@After
	public void tearDown(){
		publisher.shutdown();
	}
	
	@Test
	public void testEmptyUrl() {
		URL emptyUrl = createEmptyURL();
		List<URL> urls = Arrays.asList(emptyUrl);
		listener.notify(urls);
		
		Mockito.verify(publisher, Mockito.atLeastOnce())._notify(urls);
	}
	
	@Test
	public void testNewAddUrl(){
		RemoteSubscriberFactory factory = Mockito.mock(RemoteSubscriberFactory.class);
		URL url = createURL();
		Mockito.doReturn(factory).when(publisher).createRemoteSubscriberFactory(url, "test");
		EventSubscriber es = Mockito.mock(EventSubscriber.class);
		SubscriberGroup sg = new SubscriberGroup();
		sg.setGroupName(group);
		sg.setName("JackyLIU");
		sg.setRemoteEvents("afterAsync");
		Mockito.when(es.getSubscriberGroup(group)).thenReturn(sg);		
		Mockito.when(factory.createEventSubscriber()).thenReturn(es);
		
		PublisherGroupFactory pfactory = Mockito.mock(PublisherGroupFactory.class);
		PublisherGroup publisherGroup = new PublisherGroup(Mockito.mock(EventTransmission.class));
		Mockito.when(publisher.createPublisherGroupFactory(url, version)).thenReturn(pfactory);
		Mockito.when(pfactory.createPublisherGroup()).thenReturn(publisherGroup);
		
		
		List<URL> urls = Arrays.asList(url);
		listener.notify(urls);
		
		Assert.assertEquals(true, publisher.eventSubscribers.containsKey(version));
		Assert.assertEquals(es, publisher.eventSubscribers.get(version));
		Assert.assertEquals(true, publisher.publisherGroupFactories.containsKey(version));
		Assert.assertEquals(true, publisher.publisherGroups.containsKey(version));
		Assert.assertEquals(true, publisher.serviceProviders.containsKey("eventcenter.remote.EventSubscriber"));
		Assert.assertEquals(true, publisher.subscribEvents.containsKey(version));
	}
	
	private URL createEmptyURL(){
		return new URL("empty", "127.0.0.1", 8121);
	}
	
	private URL createURL(){
		Map<String, String> pair = new HashMap<String, String>();
		pair.put("version", version);
		pair.put("group", "test");
		pair.put(Constants.INTERFACE_KEY, EventSubscriber.class.getName());
		return new URL("dubbo", "127.0.0.1", 8121, pair);
	}
}
