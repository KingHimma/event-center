package eventcenter.builder.springschema.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class Subscriber41Main {

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/dubbo/spring-ec-dubbo-subscriber4-1.xml");
        System.out.println("启动成功，正在监听数据");
    }
}
