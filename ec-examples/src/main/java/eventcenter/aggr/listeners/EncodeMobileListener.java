package eventcenter.aggr.listeners;

import eventcenter.aggr.Student;
import eventcenter.api.EventSourceBase;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.AggregatorEventSource;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 解密手机号码的监听器
 * @author JackyLIU
 *
 */
@Component
@ListenerBind("students.query")
public class EncodeMobileListener implements AggregatorEventListener {

	@Override
	public void onObserved(EventSourceBase source) {
		AggregatorEventSource evt = (AggregatorEventSource)source;
        @SuppressWarnings("unchecked")
		List<Student> students = evt.getArg(1, List.class);
        for(Student student : students){
            student.setMobile("XXXXXXXXXXXXXXXXXXX");
        }
         
        evt.putResult(this, students);
         
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

}
