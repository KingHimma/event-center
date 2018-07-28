package eventcenter.aggr;

import eventcenter.api.EventInfo;
import eventcenter.api.aggregator.EventAggregatable;
import eventcenter.api.aggregator.simple.AbstractSimpleEventSpliter;
import eventcenter.api.aggregator.support.ListAppendResultAggregator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 并发聚合事件示例
 * @author JackyLIU
 *
 */
public class SplitMain {
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/aggr/spring-aggr-ec.xml");
        // 获取业务方法
		EventAggregatable eventCenter = ctx.getBean(EventAggregatable.class);
		
		long start = System.currentTimeMillis();
		List<Integer> list = eventCenter.fireAggregateEvent(eventCenter, new EventInfo("test2").setArgs(new Object[]{Arrays.asList(1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10)}), new AbstractSimpleEventSpliter() {
            @Override
            protected List<Object> splitArgs(Object[] args) {
                @SuppressWarnings("unchecked")
                List<Integer> _args = (List<Integer>)args[0];
                final int mode = 5;
                // 每5个元素分为一组拆分
                List<Object> list = new ArrayList<Object>();
                List<Integer> subArgs = new ArrayList<Integer>();
                for(int i = 0;i < _args.size();i++){
                    if(i != 0 && i%mode == 0){
                    	list.add(subArgs);
                    	subArgs = new ArrayList<Integer>();
                    }
                    subArgs.add(_args.get(i));
                }
                if(null != subArgs && subArgs.size() > 0){
                    list.add(subArgs);
                }
                return list;
            }
        }, new ListAppendResultAggregator<Integer>());
		
		System.out.println("took:" + (System.currentTimeMillis() - start) + "ms.result:" + list);
		
	}

}
