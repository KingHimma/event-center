package eventcenter.remote.dubbo.test;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eventcenter.api.EventInfo;
import eventcenter.api.EventCenter;
import eventcenter.api.annotation.EventPoint;
import eventcenter.api.annotation.ListenOrder;

/**
 * 监控EventPoint服务的样例
 * @author JackyLIU
 *
 */
@Service
public class SampleMonitorService {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EventCenter eventCenter;
	
	private String __execute(String param1, Integer param2){
		logger.info("方法执行了！");
		return new StringBuilder(param1).append("_").append(param2).toString();
	}
	
	/**
	 * 监控之前，同步发送事件
	 * @param param1
	 * @param param2
	 * @return
	 */
	@EventPoint(value="beforeSync", async=false, listenOrder=ListenOrder.Before )
	public String executeBeforeSync(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	@EventPoint(value="afterSync", async=false, listenOrder=ListenOrder.After )
	public String executeAfterSync(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	@EventPoint(value="beforeAsync", listenOrder=ListenOrder.Before )
	public String executeBeforeAsync(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	@EventPoint(value="afterAsync", listenOrder=ListenOrder.After )
	public String executeAfterAsync(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	@EventPoint(value="beforeDelay", listenOrder=ListenOrder.Before, delay=1000 )
	public String executeBeforeDelay(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	@EventPoint(value="afterDelay", listenOrder=ListenOrder.After, delay=1000 )
	public String executeAfterDelay(String param1, Integer param2){
		return __execute(param1, param2);
	}
	
	public String executePrivateMethod(String param1, Integer param2){
		return this.privateMethod(param1, param2);
	}
	
	public String privateMethod(String param1, Integer param2){
		String result = __execute(param1, param2);
		eventCenter.fireEvent(this, new EventInfo("privateMethod").setArgs(new Object[]{param1, param2}).setAsync(false), result);
		return result;
	}
}
