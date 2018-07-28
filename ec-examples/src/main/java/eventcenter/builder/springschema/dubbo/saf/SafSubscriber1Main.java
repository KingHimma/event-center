package eventcenter.builder.springschema.dubbo.saf;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class SafSubscriber1Main {

    public static void main(String[] args) throws IOException {
        org.apache.log4j.BasicConfigurator.configure();
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/dubbo/saf/spring-ec-dubbo-subscriber-saf1.xml");
        System.out.println("启动成功，正在监听数据");
    }
}
