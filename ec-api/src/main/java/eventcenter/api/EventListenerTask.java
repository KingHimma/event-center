package eventcenter.api;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.List;

/**
 * Created by liumingjian on 16/1/22.
 */
public class EventListenerTask implements Runnable {

    private final EventListener listener;

    private final EventSourceBase evt;

    /**
     * 是否允许抛出异常
     */
    private final Boolean allowThrowException;

    /**
     * 设置事件执行完成之后的回调,如果抛出异常也会执行此回调
     */
    private ListenerExecuted listenerExecuted;

    protected final Logger logger = Logger.getLogger(this.getClass());

    public EventListenerTask(EventListener listener, EventSourceBase evt){
        this(listener, evt, false);
    }

    public EventListenerTask(EventListener listener, EventSourceBase evt, Boolean allowThrowException){
        this.listener = listener;
        this.evt = evt;
        this.allowThrowException = allowThrowException;
    }

    /**
     * 设置事件执行完成之后的回调,如果抛出异常也会执行此回调
     * @return
     */
    public ListenerExecuted getListenerExecuted() {
        return listenerExecuted;
    }

    /**
     * 设置事件执行完成之后的回调,如果抛出异常也会执行此回调
     * @param listenerExecuted
     */
    public void setListenerExecuted(ListenerExecuted listenerExecuted) {
        this.listenerExecuted = listenerExecuted;
    }

    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        try{
            if(null != evt.getMdcValue()){
                MDC.put("clueId", evt.getMdcValue());
            }
            if (!beforeExecute()) {
                checkAndExecuteCallback();
                return;
            }
            try {
                listener.onObserved(evt);
                checkAndExecuteCallback();
            }catch(Throwable e){
                checkAndExecuteCallback(e);
                throw e;
            }
            long took = System.currentTimeMillis() - start;
            if(logger.isDebugEnabled()){
                logger.debug(new StringBuilder("execute event:").append(evt).append(" success, consumed by ").append(listener.getClass()).append(", took:").append(took).append(" ms."));
            }
            afterExecute(ListenerReceipt.buildSuc(listener, evt, start, took));
        }catch(Throwable e){
            logger.error(new StringBuilder("consumed by listener:").append(listener.getClass()).append(" failure，evt:").append(evt), e);
            afterExecute(ListenerReceipt.buildFail(listener, evt, start, e));
            if(this.allowThrowException)
                throw new RuntimeException(e);
        }finally{
            MDC.clear();
        }
    }

    private void checkAndExecuteCallback(){
        this.checkAndExecuteCallback(null);
    }

    private void checkAndExecuteCallback(Throwable e){
        if(null == listenerExecuted)
            return ;
        listenerExecuted.afterExecuted(evt, listener, e);
    }

    protected boolean beforeExecute(){
        List<ListenerFilter> globalFilters = ConfigContext.getConfig().getGlobalFilters();
        List<ListenerFilter> filters = ConfigContext.getConfig().getListenerFilters().get(evt.getEventName());
        if(null != filters && filters.size() > 0){
            for(ListenerFilter filter : filters){
                if(!beforeExecuteFilter(filter))
                    return false;
            }
        }
        if(null != globalFilters && globalFilters.size() > 0){
            for(ListenerFilter filter : globalFilters){
                if(!beforeExecuteFilter(filter))
                    return false;
            }
        }
        return true;
    }

    protected void afterExecute(ListenerReceipt receipt){
        List<ListenerFilter> globalFilters = ConfigContext.getConfig().getGlobalFilters();
        List<ListenerFilter> filters = ConfigContext.getConfig().getListenerFilters().get(evt.getEventName());
        if(null != filters && filters.size() > 0){
            for(ListenerFilter filter : filters){
                afterExecuteFilter(filter, receipt);
            }
        }
        if(null != globalFilters && globalFilters.size() > 0){
            for(ListenerFilter filter : globalFilters){
                afterExecuteFilter(filter, receipt);
            }
        }
    }

    private boolean beforeExecuteFilter(ListenerFilter filter){
        try{
            boolean result = filter.before(this.listener, this.evt);
            if(!result && logger.isDebugEnabled()){
                logger.debug(new StringBuilder("filter stop to continue consume event, eid:").append(this.evt.getEventId()).append(", event name:").append(this.evt.getEventName()).append(", filter:").append(filter.getClass()));
            }
            return result;
        }catch(Throwable e){
            logger.error(new StringBuilder("").append("before execute filter:").append(filter.getClass()).append(" failure:").append(e.getMessage()), e);
        }
        return true;
    }

    private void afterExecuteFilter(ListenerFilter filter, ListenerReceipt receipt){
        try{
            filter.after(receipt);
        }catch(Throwable e){
            logger.error(new StringBuilder("").append("after execute filter:").append(filter.getClass()).append(" failure:").append(e.getMessage()), e);
        }
    }
}
