package eventcenter.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用的事件监听器配置，如果使用了CommonEvent的模式，加载监听器，只需要在spring配置这个config即可
 * @author JackyLIU
 *
 */
public class CommonEventListenerConfig {

	private Map<String, List<EventListener>> listeners;

	public Map<String, List<EventListener>> getListeners() {
		if(null == listeners){
			listeners = new HashMap<String, List<EventListener>>();
		}
		return listeners;
	}

	/**
	 * key对应的是事件名称，value为这个事件名称下对应的listeners
	 * @param listeners
	 */
	public void setListeners(Map<String, List<EventListener>> listeners) {
		this.listeners = listeners;
	}
	
	
}
