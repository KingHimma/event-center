package eventcenter.aggr;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 并发聚合事件示例，此示例用于拆分列表聚合事件模式
 * @author JackyLIU
 *
 */
public class Main {
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/aggr/spring-aggr-ec.xml");
		// 获取业务方法
		ExampleService es = ctx.getBean(ExampleService.class);
		
		long start = System.currentTimeMillis();
		List<Student> students = es.query("Hello World");
		System.out.println("took:" + (System.currentTimeMillis() - start) + "ms.query查询结果：" + students);
		
		start = System.currentTimeMillis();
		students = es.query("Hello World");
		System.out.println("took:" + (System.currentTimeMillis() - start) + "ms.query查询结果：" + students);
		
		start = System.currentTimeMillis();
		students = es.query4ReferrenceAggregatorResult("Hello World");
		System.out.println("took:" + (System.currentTimeMillis() - start) + "ms.query4ReferrenceAggregatorResult查询结果：" + students);
	}

}
