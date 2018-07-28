package eventcenter.remote.dubbo;

import eventcenter.api.appcache.AppDataContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * 事件订阅者，也就是事件消费的服务，先启动此服务
 * @author JackyLIU
 *
 */
public class AutowiredSubscriberMain {

	/**
	 * 有两种方式进行测试，
	 * 1. 先启动SubscriberMain，然后启动PublisherMain，那么PublisherMain发送的事件将会瞬间传递过来;
	 * 2. 先启动PublisherMain，然后启动SubscriberMain，由于PublisherMain使用了SAF(离线推送机制)，那么PublisherMain未发送成功的事件，将会在很短的事件内容，重新推送到订阅端
	 *
	 * 需要在本地开启zookeeper
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
		new ClassPathXmlApplicationContext("/spring/dubbo/spring-ec-subscriber-autowired.xml");

		// 如果启动时间比较长，主要是连接到公司的ZK上需要消耗一点时间
		System.out.println("启动成功");
		
	}

}
