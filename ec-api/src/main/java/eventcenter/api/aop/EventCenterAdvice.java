package eventcenter.api.aop;

import eventcenter.api.EventCenter;
import eventcenter.api.EventInfo;
import eventcenter.api.InvalidAnnotationException;
import eventcenter.api.annotation.EventPoint;
import eventcenter.api.annotation.ListenOrder;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 通过AOP拦截器实现事件中心
 * @author JackyLIU
 *
 */
@Aspect
@Component
public class EventCenterAdvice{
	
	@Resource
	private EventCenter eventCenter;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public EventCenter getEventCenter() {
		return eventCenter;
	}

	public void setEventCenter(EventCenter eventCenter) {
		this.eventCenter = eventCenter;
	}

	@Pointcut("@annotation(eventcenter.api.annotation.EventPoint)")
    public void observeEventPoint() {
        /* pointcut definition */
    }
	
	@Around("observeEventPoint()")
	public Object around(ProceedingJoinPoint  joinPoint) throws Throwable{
		EventPoint ep = null;
		try{
			ep = findEventPoint(joinPoint);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		
		/**
		 * 监控的方法执行后的返回对象
		 */
		Object result = null;
		
		if(null == ep){
			result = joinPoint.proceed(joinPoint.getArgs());
		}else{
			// 在执行方法之前发出事件
			if(ep.listenOrder() == ListenOrder.Before){
				sendEvent(ep, joinPoint, null);
				result = joinPoint.proceed(joinPoint.getArgs());
			}else{
				// 在执行方法之后发出事件
				result = joinPoint.proceed(joinPoint.getArgs());
				result = sendEvent(ep, joinPoint, result);
			}
		}
		return result;
	}	
	
	private EventInfo createEventInfo(EventPoint ep, JoinPoint jp){
		return new EventInfo(ep.value()).setArgs(jp.getArgs()).setAsync(ep.async())
					.setDelay(ep.delay());
	}
	
	/**
	 * 发送事件，如果是同步事件，可改写返回值，异步则不能改写返回值
	 * @param ep
	 */
	private Object sendEvent(EventPoint ep, JoinPoint jp, Object result){
		return eventCenter.fireEvent(jp.getTarget(), createEventInfo(ep, jp), result);
	}
	
	/**
	 * 找到{@link EventPoint}注解，此注解只能注册在方法上
	 * @param joinPoint
	 * @return
	 * @throws NoSuchMethodException 
	 */
	private EventPoint findEventPoint(JoinPoint joinPoint) throws NoSuchMethodException{
		return getObservedMethod(joinPoint).getAnnotation(EventPoint.class);
	}
	
	private Method getObservedMethod(JoinPoint jp) throws NoSuchMethodException{
		final Signature sig = jp.getSignature();
        if (!(sig instanceof MethodSignature)) {
            throw new InvalidAnnotationException("This annotation is only valid on a method.");
        }

        final MethodSignature msig = (MethodSignature) sig;
        final Object target = jp.getTarget();

        // cannot use msig.getMethod() because it can return the method where annotation was declared i.e. method in
        // interface
        String name = msig.getName();
        Class<?>[] parameters = msig.getParameterTypes();

        return findMethodFromTargetGivenNameAndParams(target, name, parameters);
	}

    private Method findMethodFromTargetGivenNameAndParams(final Object target, final String name, final Class<?>[] parameters)
            throws NoSuchMethodException {
        Method method = target.getClass().getMethod(name, parameters);
        if(logger.isDebugEnabled()){
        	logger.debug(new StringBuilder("observe method: ").append(method));
        }
        return method;
    }
}
