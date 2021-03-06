package eventcenter.remote.saf.leveldb;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 事件发布者，也就是事件触发的服务
 * @author JackyLIU
 *
 */
public class PublisherMain {

	public static void main(String[] args) throws IOException {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
		org.apache.log4j.BasicConfigurator.configure();
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/saf/leveldb/spring-ec-publisher.xml");
		System.out.println("发布端启动成功");
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
