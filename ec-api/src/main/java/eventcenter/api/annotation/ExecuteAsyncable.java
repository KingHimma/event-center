package eventcenter.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可异步执行的监听器
 * @author JackyLIU
 *
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteAsyncable {

	/**
	 * 延迟异步执行，单位毫秒
	 * @return
	 */
	public long delay() default 0L;
}
