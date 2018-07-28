package eventcenter.remote.zk.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalPublisherMain {

	public static void main(String[] args) throws IOException {
		// 可以使用系统变量控制，或者使用dubbo.properties配置文件设置
		System.setProperty(
				"dubbo.registry.address",
				"zookeeper://localhost:2181");
		System.setProperty("dubbo.application.name", "ec-dubbo-publisher");
		System.setProperty("dubbo.service.group", "test"); // 分组一定要取号名称

		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/spring/zk/dubbo/spring-ec-publisher-local.xml");
		ExampleService es = ctx.getBean(ExampleService.class); // 获取业务方法
		System.out.println("发布端启动成功！");

		System.out
				.println("请敲入回车，调用manualFireEvent，敲入1，然后回车，调用annotationFireEvent，退出请敲入quit");

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line = reader.readLine();

		do {
			if (line.trim().equals("")) {
				es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
			} else if (line.trim().equals("1")) {
				es.annotationFireEvent("Jacky", 2); // 调用业务方法，事件在方法调用成功之后触发
			}
			line = reader.readLine();
		} while (line != null && !line.equals("quit") && !line.equals("exit"));
	}

}
