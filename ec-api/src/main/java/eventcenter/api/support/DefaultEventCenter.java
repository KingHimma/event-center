package eventcenter.api.support;

import eventcenter.api.*;
import eventcenter.api.aggregator.*;
import eventcenter.api.aggregator.simple.SimpleAggregatorContainer;
import eventcenter.api.annotation.ExecuteAsyncable;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 可分类的事件中心，也就是将同步和异步的监听器在这个方法中找出，并交给相关的方法执行
 * @author JackyLIU
 *
 */
public class DefaultEventCenter extends AbstractEventCenter implements EventAggregatable {
	
	protected ListenerCache listenerCache = ListenerCache.get();
	
	/**
	 * 聚合事件运行容器
	 */
	private AggregatorContainer aggregatorContainer;

	@Override
	@PreDestroy
	public void shutdown() throws Exception {
		super.shutdown();
		ListenerCache.clear();
	}
	
	@Override
	public Object fireEvent(Object target, EventInfo eventInfo, Object result) {
		// 先查找事件注册者
		EventRegister register = findEventRegister(eventInfo.getName());
		if(null == register){
			logger.warn(new StringBuilder("can't find event register:").append(eventInfo.getName()));
			return result;
		}

		long start = System.currentTimeMillis();
		try{
			// 先从中找出哪些是异步处理的事件，哪些需要同步处理事件，首先优先级按照实现了ExecuteAsyncable或者ExecuteSyncable接口的Listener
			// 其次使用eventInfo中的isAysnc方法
			List<EventListener> syncListeners = listenerCache.findSyncEventListeners(register, eventInfo);
			// 如果syncListeners超过1个，则需要抛出异常，如果有多个监听器使用同步调用，应该选择使用aggregator的方式调用
			if(syncListeners.size() > 1){
				throw new SyncListenerMoreThanOneException("there are one more sync listener, please use fireAggregateEvent instead");
			}

			if(syncListeners.size() == 1){
				return executeSyncListeners(target, eventInfo, result, register, syncListeners);
			}
			// 先触发异步执行事件
			executeAsyncListeners(target, eventInfo, result, register);

		}catch(SyncListenerMoreThanOneException e){
			throw e;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return result;
		}finally {
			if(logger.isTraceEnabled()){
				logger.trace("success fired event:" + eventInfo.getName() + ", took:" + (System.currentTimeMillis() - start) + " ms.");
			}
		}
		try {
			// 判断是否有IEventFireFilter，如果有，则执行filters，这里会阻塞住fireEvent，所以实现IEventFireFilter需要尽可能的减少调用延迟
			filterEventFire(target, eventInfo, result);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return result;
		}
		return result;
	}
	
	public boolean isIdle() {
		return ecConfig.getAsyncContainer().isIdle();
	}
	
	@Override
	protected long getDelay(EventInfo eventInfo, EventListener listener){
		ExecuteAsyncable asyncable = listener.getClass().getAnnotation(ExecuteAsyncable.class);
		if(null == asyncable) {
			return eventInfo.getDelay();
		}
		
		return asyncable.delay();
	}

	public void setAggregatorContainer(AggregatorContainer aggregatorContainer) {
		this.aggregatorContainer = aggregatorContainer;
	}

	@Override
	public AggregatorContainer getAggregatorContainer() {
		if(null == aggregatorContainer) {
			aggregatorContainer = new SimpleAggregatorContainer();
		}
		return aggregatorContainer;
	}
	
	protected AggregatorEventSource createAggregatorEventSource(CommonEventSource source, EventInfo eventInfo){
		if(source instanceof CommonEventSource){
			return new AggregatorEventSource(source);
		}

		return new AggregatorEventSource(source, eventInfo.getArgs(), null, getMdcValue(eventInfo));
	}

	@Override
	public <T> T fireAggregateEvent(Object target, EventInfo eventInfo, ResultAggregator<T> aggregator) {
		try{
			// 先查找事件注册者
			EventRegister register = findEventRegister(eventInfo.getName());
			if(null == register){
				logger.warn(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()));
				throw new NonExistsRegisterException(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()).toString());
			}
			
			// 先从中找出哪些是异步处理的事件，哪些是需要聚合的事件，同步事件将会被忽略
			// 然后先运行异步处理事件，然后运行聚合事件
			List<AggregatorEventListener> aggregatorListeners = listenerCache.findAggregatorEventListeners(register, eventInfo);
			
			// 先触发异步执行事件
			//executeAsyncListeners(target, eventInfo, null, register);
			
			// 如果aggregator为空，依然执行并发聚合事件，返回的结果为空
			CommonEventSource source = createEventSource(register, target, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), null, getMdcValue(eventInfo));
			return executeAggregatorEventListeners(aggregatorListeners, createAggregatorEventSource(source, eventInfo), aggregator);			
		}catch(AggregatorException e){
			throw e;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new AggregatorException(e);
		}
	}

	@Override
	public <T> T fireAggregateEvent(Object target, EventInfo eventInfo, EventSpliter spliter, ResultAggregator<T> aggregator) {
		
		try{
			EventRegister register = findEventRegister(eventInfo.getName());
			if(null == register){
				logger.warn(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()));
				throw new NonExistsRegisterException(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()).toString());
			}
			// 先从中找出哪些是异步处理的事件，哪些是需要聚合的事件，同步事件将会被忽略
			// 然后先运行异步处理事件，然后运行聚合事件
			List<AggregatorEventListener> aggregatorListeners = listenerCache.findAggregatorEventListeners(register, eventInfo);
			
			// 先触发异步执行事件
			//executeAsyncListeners(target, eventInfo, null, register);
			
			if(aggregatorListeners.size() == 0){
				throw new AggregatorException("无法找到事件：" + eventInfo.getName() + "，的聚合事件监听器");
			}
			if(aggregatorListeners.size() > 1){
				throw new AggregatorException("事件：" + eventInfo.getName() + "，并发聚合有且只能有一个监听器");
			}			
			if(null == spliter){
				throw new AggregatorException("spliter参数不能为空");
			}
			
			List<EventInfo> eis = spliter.split(target, eventInfo);
			if(null == eis || eis.size() == 0) {
				throw new AggregatorException("事件源拆分器拆分的事件信息为空");
			}
			
			List<CommonEventSource> sources = new ArrayList<CommonEventSource>();
			
			for(EventInfo ei : eis){
				sources.add(createAggregatorEventSource(
						createEventSource(register, target, ei.getId(), ei.getName(), ei.getArgs(), null, getMdcValue(eventInfo)),
						eventInfo));
			}

			CommonEventSource originalSource = createEventSource(register, target, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), null, getMdcValue(eventInfo));
			return executeSplitAggregatorEventListener(aggregatorListeners.get(0), sources, aggregator, eventInfo, originalSource);
		}catch(AggregatorException e){
			throw e;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new AggregatorException(e);
		}
	}
	
	protected <T> T executeAggregatorEventListeners(List<AggregatorEventListener> listeners, AggregatorEventSource source,
                                                    final ResultAggregator<T> aggregator){
		if(listeners.size() == 0) {
			throw new AggregatorException("listeners集合为空，至少需要一个listener");
		}
		
		if(null == getAggregatorContainer()) {
			throw new NullPointerException("无法在事件中心找到异步事件发送容器,aggregatorContainer==null");
		}
		
		try{
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("开始执行aggregator listeners, size:").append(listeners.size()).append(",event:").append(source.getEventName()));
			}
			ListenersConsumedResult result = getAggregatorContainer().executeListeners(listeners, source, new ListenerExceptionHandler(){
				@Override
				public Object handle(EventListener listener,
						CommonEventSource source, Exception e) {
					if(null == aggregator) {
						return null;
					}
					return aggregator.exceptionHandler(listener, source, e);
				}
			});
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("aggregator listeners complete:").append(source.getEventName()).append(", total took:").append(result.getTook()));
			}
			if(null == aggregator) {
				return null;
			}
			
			return aggregator.aggregate(result);
		}catch(Exception e){
			logger.error(new StringBuilder("处理并发聚合事件异常：").append(e.getMessage()), e);
			throw new AggregatorException(e);
		}
	}
	
	protected <T> T executeSplitAggregatorEventListener(AggregatorEventListener listener, List<CommonEventSource> sources, final ResultAggregator<T> aggregator, EventInfo eventInfo, CommonEventSource originalSource){
		if(null == getAggregatorContainer()) {
			throw new NullPointerException("无法在事件中心找到异步事件发送容器,aggregatorContainer==null");
		}
		
		try{
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("开始执行split aggregator listeners, size:").append(sources.size()).append(",event:").append(sources.get(0).getEventName()));
			}
			ListenersConsumedResult result = getAggregatorContainer().executeListenerSources(listener, sources, new ListenerExceptionHandler() {
				@Override
				public Object handle(EventListener listener,
									 CommonEventSource source, Exception e) {
					if (null == aggregator) {
						return null;
					}
					return aggregator.exceptionHandler(listener, source, e);
				}
			});
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder("aggregator listeners complete:").append(eventInfo.getName()).append(", total took:").append(result.getTook()));
			}
			if(null == aggregator) {
				return null;
			}
			
			result.setEventName(eventInfo.getName());
			result.setSource(originalSource);
			return aggregator.aggregate(result);
		}catch(Exception e){
			logger.error(new StringBuilder("处理并发聚合事件异常：").append(e.getMessage()), e);
			throw new AggregatorException(e);
		}
	}

	@Override
	public void directFireAggregateEvent(Object target, CommonEventSource eventSource, ListenerExceptionHandler handler) {
		EventRegister register = findEventRegister(eventSource.getEventName());
		if(null == register){
			logger.warn(new StringBuilder("无法找到事件注册者:").append(eventSource.getEventName()));
			throw new NonExistsRegisterException(new StringBuilder("无法找到事件注册者:").append(eventSource.getEventName()).toString());
		}

		_directFireAggregateEvent(target, register, eventSource, null, handler);
	}

	@Override
	public void directFireAggregateEvent(Object target, EventInfo eventInfo, ListenerExceptionHandler handler) {
		if(null == getAggregatorContainer()) {
			throw new NullPointerException("无法在事件中心找到异步事件发送容器,asyncContainer==null");
		}

		EventRegister register = findEventRegister(eventInfo.getName());
		if(null == register){
			logger.warn(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()));
			throw new NonExistsRegisterException(new StringBuilder("无法找到事件注册者:").append(eventInfo.getName()).toString());
		}

		CommonEventSource eventSource = createEventSource(register, target, eventInfo.getId(), eventInfo.getName(), eventInfo.getArgs(), null, getMdcValue(eventInfo));
		_directFireAggregateEvent(target, register, eventSource, eventInfo.getArgs(), handler);
	}

	void _directFireAggregateEvent(Object target, EventRegister register, CommonEventSource eventSource, Object[] args, ListenerExceptionHandler handler){
		try {
			if(eventSource instanceof CommonEventSource){
				eventSource = new AggregatorEventSource((CommonEventSource)eventSource);
			}else{
				eventSource = new AggregatorEventSource(eventSource, args, null, eventSource.getMdcValue());
			}
			if(handler == null){
				handler = new ListenerExceptionHandler() {
					@Override
					public Object handle(EventListener listener, CommonEventSource source, Exception e) {
						logger.error("execute listener " + listener.getClass() + ", evt:" + source + ", happened error:" + e.getMessage(), e);
						return null;
					}
				};
			}
			getAggregatorContainer().executeListeners(toAggregatorEventListeners(register.getEventListeners()), eventSource, handler);
		} catch (InterruptedException e) {
			logger.error(new StringBuilder("处理并发聚合事件异常：").append(e.getMessage()), e);
			throw new AggregatorException(e);
		}
	}

	List<AggregatorEventListener> toAggregatorEventListeners(EventListener[] listeners){
		List<AggregatorEventListener> list = new ArrayList<AggregatorEventListener>();
		for(EventListener listener : listeners){
			list.add(new AggregatorListenerWrapper(listener));
		}
		return list;
	}
}
