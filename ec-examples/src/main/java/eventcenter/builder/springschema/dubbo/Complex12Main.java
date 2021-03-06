package eventcenter.builder.springschema.dubbo;

import eventcenter.builder.ExampleService;
import eventcenter.builder.springschema.BaseMain;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class Complex12Main extends BaseMain {

    public static void main(String[] args) throws IOException {
        //org.apache.log4j.BasicConfigurator.configure();

        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/dubbo/spring-ec-dubbo-complex1-2.xml");
        ExampleService es = ctx.getBean(ExampleService.class);
        openCommand(es);
    }
}
