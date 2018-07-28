package eventcenter.remote.zk.dubbo;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * 事件订阅者，也就是事件消费的服务，先启动此服务
 * @author JackyLIU
 *
 */
public class Subscriber2Main {

	/**
	 *  @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// 可以使用系统变量控制，或者使用dubbo.properties配置文件设置
		System.setProperty("dubbo.registry.address", "zookeeper://localhost:2181");
		System.setProperty("dubbo.application.name", "ec-dubbo-consumer");
		System.setProperty("dubbo.service.group", "test");	//分组名称必须要发布端的分组名称一致
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, System.getProperty("user.home") + File.pathSeparator + "subsriber2");
				
		new ClassPathXmlApplicationContext("/spring/zk/dubbo/spring-ec-subscriber2.xml");
				
		System.out.println("订阅器2启动成功");			// 如果启动时间比较长，主要是连接到公司的ZK上需要消耗一点时间
		
	}

}
