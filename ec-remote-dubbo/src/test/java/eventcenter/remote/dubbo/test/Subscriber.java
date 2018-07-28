package eventcenter.remote.dubbo.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Subscriber {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring/spring-ec-subscriber.xml");
		ctx.getBean("sampleMonitorService", SampleMonitorService.class);
		System.out.println("启动监听器成功！");
		ctx.close();
	}

}
