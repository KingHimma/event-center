package eventcenter.builder.springschema.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class Subscriber1Main {

    public static void main(String[] args) throws IOException {
        org.apache.log4j.BasicConfigurator.configure();
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/dubbo/spring-ec-dubbo-subscriber1.xml");
        System.out.println("启动成功，正在监听数据");
    }
}
