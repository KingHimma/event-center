package eventcenter.builder.springschema;

import eventcenter.builder.ExampleService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author liumingjian
 * @date 2018/5/3
 **/
public class BaseMain {

    protected static void openCommand(ExampleService es) throws IOException {
        System.out.println("请敲入回车，调用manualFireEvent，敲入1，然后回车，调用annotationFireEvent，退出请敲入quit");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();

        do{
            if(line.trim().equals("")){
                es.manualFireEvent("Hello", 1);// 调用业务方法，调用成功之后，将会触发事件
            }else if(line.trim().equals("1")){
                es.annotationFireEvent("Jacky", 2);	// 调用业务方法，事件在方法调用成功之后触发
            }
            line = reader.readLine();
        }while(line != null && !line.equals("quit") && !line.equals("exit"));
    }
}
