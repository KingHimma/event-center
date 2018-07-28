package eventcenter.builder.springschema.dubbo;

import eventcenter.builder.ExampleService;
import eventcenter.builder.springschema.BaseMain;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class Publish4Main extends BaseMain {

    public static void main(String[] args) throws IOException {
        org.apache.log4j.xml.DOMConfigurator.configure(Publish4Main.class.getResource("/log4j.xml"));
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/dubbo/spring-ec-dubbo-publisher4.xml");
        ExampleService es = ctx.getBean(ExampleService.class);
        openCommand(es);
    }
}
