package eventcenter.builder;

import eventcenter.DateUtils;
import eventcenter.api.EventCenter;
import eventcenter.api.EventInfo;
import eventcenter.api.annotation.EventPoint;
import eventcenter.api.annotation.ListenOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 示例服务，里面的方法将会触发事件
 * @author JackyLIU
 *
 */
@Service
public class ExampleService {

	/**
	 * 手动触发事件时，需要引入此接口，如果使用Spring框架，可以使用@Resource标签注入进来
	 */
	@Resource
	private EventCenter eventCenter;

	public EventCenter getEventCenter() {
		return eventCenter;
	}

	public void setEventCenter(EventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}

	/**
	 * 手动触发时间，事件将会在代码块中显示调用fireEvent方法
	 * @param data1
	 * @param data2
	 */
	public void manualFireEvent(String data1, Integer data2){
		System.out.println(DateUtils.getNowDate() + "manualFireEvent-方法执行成功：" + data1 + "," + data2);
		// 以下将会触发方法，fireEvent无论如何都不会抛出异常，所以不需要try catch
		eventCenter.fireEvent(this, new EventInfo("example.manual").setArgs(new Object[]{data1, data2}), "传递方法执行的结果对象，也可以将这个参数放入到args中");
	}
	
	/**
	 * 使用注解触发方法
	 * @param data1
	 * @param data2
	 * @return
	 */
	@EventPoint(value="example.annotation", async=true/*默认为true*/, listenOrder=ListenOrder.After/*默认在方法调用完成之后发送事件，After将会把方法返回的结果传入到事件源的result中*/ )
	public String annotationFireEvent(String data1, Integer data2){
		System.out.println(DateUtils.getNowDate() + "annotationFireEvent-方法执行成功：" + data1 + "," + data2);
		return "业务方法返回的结果";
	}
	
	/**
	 * 使用阻塞式的事件消费，默认的事件容器使用的是异步消费，同步只适合同一个JVM消费，不能使用远程事件
	 * @param data1
	 * @param data2
	 * @return
	 */
	@EventPoint(value="example.annotation.sync", async=false/*这里设置为false*/, listenOrder=ListenOrder.After/*默认在方法调用完成之后发送事件，After将会把方法返回的结果传入到事件源的result中*/ )
	public String annotationSyncFireEvent(String data1, Integer data2){
		System.out.println(DateUtils.getNowDate() + "annotationFireEvent-方法执行成功：" + data1 + "," + data2);
		return "业务方法返回的结果";
	}
	
	
}
