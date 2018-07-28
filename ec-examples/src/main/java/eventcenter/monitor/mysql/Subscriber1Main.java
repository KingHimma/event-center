package eventcenter.monitor.mysql;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * 事件订阅者，也就是事件消费的服务，先启动此服务
 * @author JackyLIU
 *
 */
public class Subscriber1Main {

	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		System.setProperty("dubbo.registry.address", "zookeeper://localhost:2181");
		System.setProperty("dubbo.application.name", "ec-dubbo-subscriber");
		System.setProperty("dubbo.service.group", "test");	//分组一定要取号名称
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target" + File.separator + "monitor" + File.separator + "sub");
		new ClassPathXmlApplicationContext("/spring/monitor/mysql/spring-ec-subscriber1.xml");
		
		System.out.println("订阅端启动成功");			// 如果启动时间比较长，主要是连接到公司的ZK上需要消耗一点时间
		
	}

}
