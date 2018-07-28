package eventcenter.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eventcenter.api.EventListener;
import eventcenter.api.EventRegister;
import eventcenter.api.EventListener;
import eventcenter.api.EventRegister;

/**
 * 这个注解是用于解释{@link EventListener}实现类中，加上这个注解，Spring
 * 的applicationContext初始化完毕之后，将会自动的将有这个注解的{@link EventListener}
 * 注入到{@link EventRegister}中
 * @author JackyLIU
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenerBind {

	/**
	 * 事件名称，如果这个事件绑定了多个事件名称，那么使用','分割
	 * @return
	 */
	public String value();
}
