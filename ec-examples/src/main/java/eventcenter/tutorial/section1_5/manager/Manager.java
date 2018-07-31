package eventcenter.tutorial.section1_5.manager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 管理层的员工
 * @author liumingjian
 * @date 2018/7/29
 **/
@Component
public class Manager implements Serializable {
    private static final long serialVersionUID = -5397461665049816937L;

    private final Logger logger = Logger.getLogger(this.getClass());

    @Value("管理A")
    String name;

    /**
     * 安排老板的任务
     * @param content
     */
    public void manageTask(String bossName, String content){
        logger.info(name + "开始安排" + bossName + "分配的任务:" + content);
    }
}
