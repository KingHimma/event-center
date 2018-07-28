package eventcenter.monitor.server.controller;

import eventcenter.monitor.server.model.ExecutedResponse;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by liumingjian on 16/2/25.
 */
@Component
@Aspect
public class GlobalHandler {

    final Logger logger = Logger.getLogger(this.getClass());

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        final long start = System.currentTimeMillis();
        try {
            result = joinPoint.proceed(joinPoint.getArgs());
            // 一般响应到页面时，返回的内容一般都是字符串类型
            if (result instanceof String)
                return result;

            if (result instanceof ExecutedResponse){
                return result;
            }
            return ExecutedResponse.buildSuccess(System.currentTimeMillis() - start, result);
        } catch (Exception e) {
            return ExecutedResponse.buildError(System.currentTimeMillis() - start, e.getMessage());
        }
    }
}
