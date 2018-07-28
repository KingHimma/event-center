package eventcenter.api;

import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.annotation.ExecuteAsyncable;
import eventcenter.api.annotation.ExecuteSyncable;
import eventcenter.api.annotation.ExecuteAsyncable;
import eventcenter.api.annotation.ExecuteSyncable;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * To be cache listeners map with event's name, so it could be quick to find listeners by event name.
 * @author JackyLIU
 *
 */
public class ListenerCache {
	
	private static ListenerCache __self;
	
	private ListenerCache(){}
	
	public static synchronized ListenerCache get(){
		if(null == __self)
			__self = new ListenerCache();
		return __self;
	}

	public static synchronized void clear(){
		if(null == __self)
			return ;
		__self.asyncListeners.clear();
		__self.syncListeners.clear();
	}
	
	/**
	 * 异步监听器缓存
	 */
	Map<String, List<EventListener>> asyncListeners = Collections.synchronizedMap(new HashMap<String, List<EventListener>>());

	/**
	 * 同步监听器缓存
	 */
	Map<String, List<EventListener>> syncListeners = Collections.synchronizedMap(new HashMap<String, List<EventListener>>());

	/**
	 * 聚合监听器缓存
	 */
	Map<String, List<AggregatorEventListener>> aggregatorListeners = Collections.synchronizedMap(new HashMap<String, List<AggregatorEventListener>>());
	
	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * 找到异步的监听器
	 * @return
	 */
	public List<EventListener> findAsyncEventListeners(EventRegister register, EventInfo eventInfo){
		if(asyncListeners.containsKey(eventInfo.getName()))
			return asyncListeners.get(eventInfo.getName());

		List<EventListener> __asyncListeners = new ArrayList<EventListener>();
		if(!eventInfo.isAsync() && eventInfo.getDelay() == 0){
			asyncListeners.put(eventInfo.getName(), __asyncListeners);
			return __asyncListeners;
		}
		
		List<EventListener> listeners = filterAggregatorEventListeners(register.getEventListeners());
		for(EventListener listener : listeners){
			ExecuteSyncable syncable = listener.getClass().getAnnotation(ExecuteSyncable.class);
			if(null != syncable){
				continue;
			}		

			__asyncListeners.add(listener);
		}
		
		asyncListeners.put(eventInfo.getName(), __asyncListeners);
		return __asyncListeners;
	}
	
	public List<EventListener> findAsyncEventListeners(EventRegister register, String eventName){
		if(asyncListeners.containsKey(eventName))
			return asyncListeners.get(eventName);
		
		List<EventListener> listeners = filterAggregatorEventListeners(register.getEventListeners());
		List<EventListener> __asyncListeners = new ArrayList<EventListener>();
		for(EventListener listener : listeners){
			ExecuteSyncable syncable = listener.getClass().getAnnotation(ExecuteSyncable.class);
			if(null != syncable){
				continue;
			}		

			__asyncListeners.add(listener);
		}
		
		asyncListeners.put(eventName, __asyncListeners);
		return __asyncListeners;
	}
	
	/**
	 * 过滤掉{@link AggregatorEventListener}，这里只处理{@link EventListener}
	 * @param listeners
	 * @return
	 */
	public List<EventListener> filterAggregatorEventListeners(EventListener[] listeners){
		if(null == listeners)
			return new ArrayList<EventListener>();
		List<EventListener> filterList = new ArrayList<EventListener>(listeners.length);
		for(EventListener listener : listeners){
			if(!(listener instanceof AggregatorEventListener)){
				filterList.add(listener);
			}
		}
		return filterList;
	}
	
	/**
	 * 找到同步监听器
	 * @param register
	 * @param eventInfo
	 * @return
	 */
	public List<EventListener> findSyncEventListeners(EventRegister register, EventInfo eventInfo){
		if(syncListeners.containsKey(eventInfo.getName()))
			return syncListeners.get(eventInfo.getName());
		
		List<EventListener> listeners = filterAggregatorEventListeners(register.getEventListeners());
		List<EventListener> __syncListeners = new ArrayList<EventListener>();
		for(EventListener listener : listeners){
			ExecuteAsyncable asyncable = listener.getClass().getAnnotation(ExecuteAsyncable.class);
			ExecuteSyncable syncable = listener.getClass().getAnnotation(ExecuteSyncable.class);
			if(null != asyncable && null != syncable){
				logger.warn(new StringBuilder(listener.getClass().getName()).append("同时标注了ExecuteAsyncable和ExecuteSyncable，这里将会默认使用ExecuteAsyncable"));
			}else if(null != syncable){
				__syncListeners.add(listener);
			}else if(!eventInfo.isAsync() && eventInfo.getDelay() == 0L){
				__syncListeners.add(listener);
			}
		}
		
		syncListeners.put(eventInfo.getName(), __syncListeners);
		return __syncListeners;
	}
	
	public List<AggregatorEventListener> findAggregatorEventListeners(EventRegister register, EventInfo eventInfo){
		if(aggregatorListeners.containsKey(eventInfo.getName())){
			return aggregatorListeners.get(eventInfo.getName());
		}
		
		EventListener[] listeners = register.getEventListeners();
		if(null == listeners)
			return new ArrayList<AggregatorEventListener>();
		
		List<AggregatorEventListener> __aggregatorListeners = new ArrayList<AggregatorEventListener>();
		for(EventListener listener : listeners){
			if(listener instanceof AggregatorEventListener){
				__aggregatorListeners.add((AggregatorEventListener)listener);
			}
		}
		
		aggregatorListeners.put(eventInfo.getName(), __aggregatorListeners);
		return __aggregatorListeners;
	}
}
