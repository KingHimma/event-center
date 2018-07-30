package eventcenter.tutorial.section1_2;

import eventcenter.api.EventCenter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    /**
     * 在Boss类中，引用eventCenter，让Boss具备发送事件的能力，后续章节，可以通过注解的方式发送事件，减少代码的入侵性
     */
    @Resource
    EventCenter eventCenter;

    @Value("大老板")
    String name;

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * 老板提交任务的方法
     * @param content
     */
    public void submitTask(String content){
        logger.info(name + "创建了任务了:" + content);
        // 老板任务创建完成之后，触发了boss.task.submit的事件，并将老板的name放在第一个参数中，content放在第二个参数中
        eventCenter.fireEvent(this, "boss.task.submit", name, content);
    }
}
