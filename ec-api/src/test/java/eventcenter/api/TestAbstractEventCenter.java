package eventcenter.api;

import java.util.HashMap;
import java.util.Map;

import eventcenter.api.async.simple.SimpleQueueEventContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eventcenter.api.async.simple.SimpleQueueEventContainer;

/**
 * 测试{@link AbstractEventCenter}
 * @author JackyLIU
 *
 */
public class TestAbstractEventCenter {

	private AbstractEventCenter ec;
	
	@Before
	public void setUp(){
		ec = new AbstractEventCenter(){
			
			@Override
			public Object fireEvent(Object target, EventInfo eventInfo,
					Object result) {
				return null;
			}

			@Override
			protected long getDelay(EventInfo eventInfo, EventListener listener) {
				return 0;
			}
		};
	}
	
	@After
	public void tearDown() throws Exception{
		ec.shutdown();
	}
	
	@Test
	public void testStartup1() throws Exception{
		ec.startup();
		EventContainer container = ec.getAsyncContainer();
		Assert.assertEquals(true, container instanceof SimpleQueueEventContainer);
	}
	
	@Test
	public void testStartup2(){
		EventCenterConfig config = new EventCenterConfig();
		Map<String, EventRegister> registers = new HashMap<String, EventRegister>();
		CommonEventRegister register = new CommonEventRegister();
		register.setEventListeners(new EventListener[]{new EventListener(){

			@Override
			public void onObserved(EventSourceBase source) {
				
			}
			
		}});
		registers.put("event.test", register);
		config.setEventRegisters(registers);
		ec.setEcConfig(config);
		EventContainer container = ec.getAsyncContainer();
		Assert.assertEquals(true, container instanceof SimpleQueueEventContainer);	
		
		Assert.assertNotNull(ec.getEventCenterConfig().getEventRegisters().get("event.test"));
	}
	
	
}
