package eventcenter.leveldb.tx;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	/**
	 * 启动这个程序之前，请先启动{@link StopMain}方法
	 * @param args
	 */
	public static void main(String[] args) {
		//org.apache.log4j.BasicConfigurator.configure();
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/leveldb/tx/spring-ec.xml");
		// 等待执行
	}

}
