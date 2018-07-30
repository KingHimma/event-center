package eventcenter.tutorial.section1_3;

import eventcenter.api.annotation.EventPoint;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 领域模型：老板 (Boss)
 *
 * @author liumingjian
 * @date 2018/7/29
 **/
@Component
public class Boss implements Serializable {
    private static final long serialVersionUID = 1905777591870557841L;

    @Value("大老板")
    String name;

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * <pre>
     * 老板提交任务的方法。
     * @EventPoint注解可以拦截方法完成之前或者之后，触发一次fireEvent的操作，也就是发事件的操作。
     * 老板任务创建完成之后，将会触发boss.task.submit的事件，事件的参数将会和submitTask方法中的参数保持一致，方法返回的result将会放入事件信息的result属性中
     * </pre>
     * @param content
     */
    @EventPoint(value = "boss.task.submit")
    public String submitTask(String content){
        logger.info(name + "创建了任务了:" + content);
        // 返回一些结果，这些结果将会被带入到事件信息的result字段中
        return name;
    }
}
