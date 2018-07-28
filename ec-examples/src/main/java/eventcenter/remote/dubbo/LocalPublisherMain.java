package eventcenter.remote.dubbo;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class LocalPublisherMain {

	public static void main(String[] args) {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/dubbo/spring-ec-publisher-local.xml");
		ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法
		
		es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
		// 查看SubscriberMain的控制台吧..
		
		// 接下来调用注解触发事件
		es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
	}

}
