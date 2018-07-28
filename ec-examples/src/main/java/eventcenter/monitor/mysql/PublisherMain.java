package eventcenter.monitor.mysql;

import eventcenter.api.appcache.AppDataContext;
import eventcenter.monitor.ExampleService;
import eventcenter.monitor.Order;
import eventcenter.monitor.Trade;
import org.apache.log4j.MDC;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 事件发布者，也就是事件触发的服务
 * @author JackyLIU
 *
 */
public class PublisherMain {

	public static void main(String[] args) throws IOException {
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target" + File.separator + "monitor" + File.separator + "pub");
		//org.apache.log4j.BasicConfigurator.configure();
		System.setProperty("dubbo.registry.address", "zookeeper://localhost:2181");
		System.setProperty("dubbo.application.name", "ec-dubbo-publisher");
		System.setProperty("dubbo.service.group", "test");	//分组一定要取号名称
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/monitor/mysql/spring-ec-publisher.xml");
		System.out.println("发布端启动成功");
		ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法


		int count = 0;
		System.out.println("正在开始执行压力测试");
		for(int i = 0;i < count;i++){
			handleCommand("", es);
		}
		System.out.println("压力测试结束，总共发送事件数:" + count);

		System.out.println("请敲入回车，调用manualFireEvent，敲入1，然后回车，调用annotationFireEvent，退出请敲入quit");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = reader.readLine();

		do{
			handleCommand(line, es);
			line = reader.readLine();
		}while(line != null && !line.equals("quit") && !line.equals("exit"));
	}

	private static void handleCommand(String line, ExampleService es){
		MDC.put("clueId", UUID.randomUUID().toString());
		if(line.trim().equals("")){
			es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
		}else if(line.trim().equals("1")){
			es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
		}else if(line.trim().startsWith("2")){
			Trade t = createTrade(null, null, null);
			es.consignTrade(t);
		}
	}

	private static Trade createTrade(String buyerNick, String sellerNick, String status){
		Trade t = new Trade();
		t.setBuyerNick(buyerNick);
		t.setId(UUID.randomUUID().toString());
		t.setTid(UUID.randomUUID().toString());
		t.setStatus(status);
		t.setOutSid(UUID.randomUUID().toString());
		t.setSellerNick(sellerNick);
		List<Order> orders = new ArrayList<Order>();
		Order order = new Order();
		order.setId(UUID.randomUUID().toString());
		order.setTid(t.getTid());
		order.setNumIid(UUID.randomUUID().toString());
		order.setTitle("测试");
		orders.add(order);
		t.setOrders(orders);
		return t;
	}
}
