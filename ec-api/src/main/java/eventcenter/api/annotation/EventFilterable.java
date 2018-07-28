package eventcenter.api.annotation;

import eventcenter.api.ListenerFilter;
import eventcenter.api.ListenerFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It should be tag on class which implemented {@link ListenerFilter}
 * Created by liumingjian on 16/1/26.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventFilterable {

    /**
     * 声明这个过滤器需要和哪些事件进行关联，如果要关联多个事件，请使用','分割。
     * @return
     */
    public String value() default "";

    /**
     * 是否为全局过滤器，全局过滤器将会为每个事件都执行一遍过滤，如果isGlobal为false，并且value为空字符串，那么启动事件中心将会报错
     * @return
     */
    public boolean isGlobal() default false;
}
