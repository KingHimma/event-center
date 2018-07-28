package eventcenter.builder.springschema.local;

import eventcenter.aggr.ExampleService;
import eventcenter.aggr.Student;
import eventcenter.api.support.DefaultEventCenter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class Aggr3Main {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/local/spring-ec-aggr3.xml");
        DefaultEventCenter eventCenter = ctx.getBean(DefaultEventCenter.class);
        ExampleService es = ctx.getBean(ExampleService.class);
        List<Student> students = es.query("test");
        System.out.println(students);
        eventCenter.shutdown();
    }
}
