package eventcenter.builder.monitor;

import eventcenter.api.appcache.AppDataContext;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.ExampleService;
import eventcenter.builder.InitBuilder;
import eventcenter.builder.dubbo.DubboConfigContext;
import eventcenter.builder.dubbo.DubboPublisherConfigBuilder;
import eventcenter.builder.monitor.log.LogMonitorConfigBuilder;
import org.apache.log4j.MDC;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by liumingjian on 2017/9/15.
 */
public class LogMonitorMain {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");

        DubboConfigContext.getInstance().applicationName("ec-sample-publisher").registryProtocol("zookeeper").registryAddress("localhost:2181").groupName("test");
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .publisher(new DubboPublisherConfigBuilder()
                        .addLocalPublisherGroup("example.manual")
                        .devMode(true)
                        .subscriberAutowired(true)
                        .build())
                .monitor(new LogMonitorConfigBuilder()
                        .heartbeatInterval(1000L)
                        .build()).build();
        eventCenter.startup();
        ExampleService es = new ExampleService();
        es.setEventCenter(eventCenter);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();

        do{
            handleCommand(line, es);
            line = reader.readLine();
        }while(line != null && !line.equals("quit") && !line.equals("exit"));
        eventCenter.shutdown();
    }

    private static void handleCommand(String line, ExampleService es){
        MDC.put("clueId", UUID.randomUUID().toString());
        if(line.trim().equals("")){
            es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
        }else if(line.trim().equals("1")){
            es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
        }
    }
}
