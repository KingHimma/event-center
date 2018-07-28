package eventcenter.leveldb;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/leveldb/spring-ec.xml");
		// 获取业务方法
		ExampleService es = ctx.getBean(ExampleService.class);

		// 调用业务方法，调用成功之后，将会触发事件
		es.manualFireEvent("Hello", 1);
		// 等待控制台的事件触发吧。。。。
		
		// 接下来调用注解触发事件
		// 调用业务方法，事件在方法调用成功之后触发
		es.annotationFireEvent("Jacky", 2);
		// 等待控制台的事件触发吧。。。。

		// 调用业务方法，此事件发送，使用的是阻塞式
		es.annotationSyncFireEvent("HuGui", 3);
		// 等待控制台的事件触发吧。。。。
	}

}
