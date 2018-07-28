package eventcenter.local;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/local/spring-ec.xml");
		ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法
		
		es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
		// 等待控制台的事件触发吧。。。。
		
		// 接下来调用注解触发事件
		es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
		// 等待控制台的事件触发吧。。。。
		
		es.annotationSyncFireEvent("HuGui", 3);	// 调用业务方法，此事件发送，使用的是阻塞式
		// 等待控制台的事件触发吧。。。。
	}

}
