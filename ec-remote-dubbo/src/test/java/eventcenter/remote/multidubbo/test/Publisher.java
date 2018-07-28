package eventcenter.remote.multidubbo.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import eventcenter.remote.dubbo.test.SampleMonitorService;

public class Publisher {

	public static void main(String[] args) throws IOException {
		org.apache.log4j.BasicConfigurator.configure();
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-multi/spring-ec-publisher.xml");
		SampleMonitorService service = ctx.getBean("sampleMonitorService", SampleMonitorService.class);
		System.out.println("启动发布者成功！");
		
		waitInput(service);
		ctx.close();
	}
	
	private static void waitInput(SampleMonitorService service) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		int count = 0;
		while((line = reader.readLine()) != null){
			String command = line.trim().toLowerCase();
			if(command.equals("quit") || command.equals("exit")){
				System.exit(0);
			}
			if(command.equals("s")){
				service.executeBeforeAsync("测试" + count, count);
				service.executeAfterAsync("测试" + count, count);
				count++;
			}
		}
	}

}
