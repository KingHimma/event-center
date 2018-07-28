package eventcenter.local.listeners;

import eventcenter.DateUtils;
import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

/**
 * 同时订阅多个事件
 * @author JackyLIU
 *
 */
@Component	// 如果使用Spring，需要设置spring的context-scan，并将这个包配置到扫描包中
@ListenerBind("example.manual,example.annotation") // 设置监控的事件名称，使用,注册多个事件
public class MultiSubscribEventListener implements EventListener {

	@Override
	public void onObserved(CommonEventSource source) {
		CommonEventSource evt = source;	// 默认的事件源使用的是CommonEventSource，当然EventSourceBase是可以定制的，请参考文档，建议使用默认方式
		String data1 = evt.getArg(0, String.class); // 获取事件参数，下标从0开始
		Integer data2 = evt.getArg(1, Integer.class);	
		String result = evt.getResult(String.class);	// 获取事件结果，可以把它理解为，事件参数的另一种形式
		
		System.out.println(DateUtils.getNowDate() + this.getClass() + "消费了事件: " + evt.getEventName() /*获取事件名称*/ + "，参数：" + data1 + "_" + data2 + "_" + result);
	}

}
