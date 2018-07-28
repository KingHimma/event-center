package eventcenter.remote.dubbo.test;

import java.util.List;

import eventcenter.remote.EventTransmission;
import eventcenter.remote.publisher.PublisherGroup;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eventcenter.remote.publisher.PublisherGroup;

public class Publisher {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring/spring-ec-publisher.xml");
		SampleMonitorService service = ctx.getBean("sampleMonitorService", SampleMonitorService.class);
		System.out.println("启动发布者成功！");
		
		service.executeBeforeAsync("测试1", 1);
		
		EventTransmission eventTrans = ((PublisherGroup)ctx.getBean("publisherGroups", List.class).get(0)).getEventTransmission();
		try{
			System.out.println("checkHealth:" + eventTrans.checkHealth());
		}catch(Exception e){
			System.err.println("checkHealth failure:" + e.getMessage());
		}
		ctx.close();
	}

}
