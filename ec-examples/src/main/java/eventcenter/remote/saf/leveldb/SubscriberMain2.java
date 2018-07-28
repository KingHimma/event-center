package eventcenter.remote.saf.leveldb;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * 事件订阅者，也就是事件消费的服务，先启动此服务
 * @author JackyLIU
 *
 */
public class SubscriberMain2 {

	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
		new ClassPathXmlApplicationContext("/spring/saf/leveldb/spring-ec-subscriber2.xml");
		
		System.out.println("订阅端启动成功");			// 如果启动时间比较长，主要是连接到公司的ZK上需要消耗一点时间
		
	}

}
