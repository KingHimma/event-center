package eventcenter.leveldb.tx;

import eventcenter.api.EventCenter;
import eventcenter.api.EventContainer;
import eventcenter.api.support.DefaultEventCenter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StopMain {

	public static void main(String[] args) {
		//org.apache.log4j.BasicConfigurator.configure();
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/leveldb/tx/spring-ec.xml");
		ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法

		DefaultEventCenter eventCenter = (DefaultEventCenter)ctx.getBean(EventCenter.class);
		EventContainer container = eventCenter.getAsyncContainer();
		final int count = 100;
		for(int i = 0;i < count;i++){
			es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
		}
		System.out.println("总共发送事件:" + count + ", countOfQueueBuffer:" + container.countOfQueueBuffer() + ",countOfLiveThread:" + container.countOfLiveThread() + ",queueSize:" + container.queueSize());
		System.exit(0);
	}

}
