package eventcenter.remote.dubbo.publisher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;

/**
 * 测试{@link DubboRegistryEventPublisher}的startup方法
 * @author JackyLIU
 *
 */
public class TestPublisherStartup {

	private DubboRegistryEventPublisher publisher;
	
	private RegistryService registryService;
	
	@Before
	public void setUp(){
		registryService = Mockito.mock(RegistryService.class);
		publisher = new DubboRegistryEventPublisher();
		System.setProperty(DubboRegistryEventPublisher.APPLICATION_NAME, "publisherTest");
		System.setProperty(DubboRegistryEventPublisher.REGISTRY_ADDRESS, "localhost:2181");
		publisher.setDubboGroup("test");		
		publisher.setRegistryService(registryService);
	}
	
	@Test
	public void test() {
		publisher.startup();
		
		Mockito.verify(registryService, Mockito.atLeastOnce()).subscribe(Mockito.any(URL.class), Mockito.any(NotifyListener.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentException(){
		publisher.setDubboGroup(null);
		publisher.startup();
		
		
	}

}
