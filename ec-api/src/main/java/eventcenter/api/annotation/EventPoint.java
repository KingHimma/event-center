package eventcenter.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件点注解，将需要发出事件的方法打上标签,以便获取事件
 * @author JackyLIU
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventPoint{

	/**
	 * 定义事件的名称，建议使用英文，名称的格式为"domain"."name"，最好定义domain 
	 * @return 事件的名称
	 */
	String value() default "";
	
	/**
	 * 是否需要异步处理事件，默认为true，如果是同步事件，可改写返回值，异步则不能改写返回值
	 * @return
	 */
	boolean async() default true;
	
	/**
	 * 是否需要延迟执行，默认为0，表示不延迟执行，如果设置了delay>0，那么async无论是什么值，都将异步执行
	 * @return
	 */
	long delay() default 0;
	
	/**
	 * 监控顺序，默认在监控点之后发出事件
	 * @return
	 */
	ListenOrder listenOrder() default ListenOrder.After;
}
