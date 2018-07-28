package eventcenter.scheduler.quartz;

import java.util.HashMap;
import java.util.Map;

import eventcenter.scheduler.CronEventTrigger;
import eventcenter.scheduler.SimpleEventTrigger;

/**
 * 触发器解析器注册机
 * @author JackyLIU
 *
 */
public class ResolverRegister {

	private Map<String, EventTriggerResolver> resolvers;
	
	private static ResolverRegister self;
	
	private ResolverRegister(){}
	
	public static ResolverRegister getInstance(){
		if(null == self){
			self = new ResolverRegister();
		}
		return self;
	}

	public Map<String, EventTriggerResolver> getResolvers() {
		if(null == resolvers){
			resolvers = new HashMap<String, EventTriggerResolver>();
		}
		if(!resolvers.containsKey(SimpleEventTriggerResolver.class.getName())){
			resolvers.put(SimpleEventTrigger.class.getName(), new SimpleEventTriggerResolver());
		}
		if(!resolvers.containsKey(CronEventTriggerResolver.class.getName())){
			resolvers.put(CronEventTrigger.class.getName(), new CronEventTriggerResolver());
		}
		return resolvers;
	}

	public void setResolvers(
			Map<String, EventTriggerResolver> resolvers) {
		this.resolvers = resolvers;
	}
}
