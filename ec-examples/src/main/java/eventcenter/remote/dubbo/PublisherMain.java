package eventcenter.remote.dubbo;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;


/**
 * 事件发布者，也就是事件触发的服务
 *
 * 如果启动报了：Can't assign requested address
 * 则在启动参数中，添加虚拟机参数：-Djava.net.preferIPv4Stack=true
 * @author JackyLIU
 *
 */
public class PublisherMain {

	public static void main(String[] args) {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/dubbo/spring-ec-publisher.xml");
		// 获取业务方法
		ExampleService es = ctx.getBean(ExampleService.class);

		// 调用业务方法，调用成功之后，将会触发事件
		es.manualFireEvent("Hello", 1);
		// 查看SubscriberMain的控制台吧..
		
		// 接下来调用注解触发事件
		// 调用业务方法，事件在方法调用成功之后触发
		es.annotationFireEvent("Jacky", 2);
		// 查看SubscriberMain的控制台吧..
	}

}
