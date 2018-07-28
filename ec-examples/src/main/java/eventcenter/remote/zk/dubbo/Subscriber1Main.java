package eventcenter.remote.zk.dubbo;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 事件订阅者，也就是事件消费的服务，先启动此服务，他将会自动连接到发布端，并订阅相关的事件
 * @author JackyLIU
 *
 */
public class Subscriber1Main {

	/**
	 *  @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		// 可以使用系统变量控制，或者使用dubbo.properties配置文件设置
		System.setProperty("dubbo.registry.address", "zookeeper://localhost:2181");
		System.setProperty("dubbo.application.name", "ec-dubbo-consumer");
		System.setProperty("dubbo.service.group", "test");	//分组名称必须要发布端的分组名称一致
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, System.getProperty("user.home") + File.separator + "subsriber1");
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/zk/dubbo/spring-ec-subscriber1.xml");
		
		System.out.println("订阅器1启动成功");			// 如果启动时间比较长，主要是连接到公司的ZK上需要消耗一点时间
		ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法

		System.out.println("请敲入回车，调用manualFireEvent，敲入1，然后回车，调用annotationFireEvent，退出请敲入quit");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = reader.readLine();
		
		do{
			if(line.trim().equals("")){
				es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
			}else if(line.trim().equals("1")){
				es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
			}
			line = reader.readLine();
		}while(line != null && !line.equals("quit") && !line.equals("exit"));
		
	}

}
