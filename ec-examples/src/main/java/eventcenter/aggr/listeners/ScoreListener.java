package eventcenter.aggr.listeners;

import eventcenter.aggr.Student;
import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.AggregatorEventSource;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 计算分数监听器
 * @author JackyLIU
 *
 */
@Component
@ListenerBind("students.query")
public class ScoreListener implements AggregatorEventListener {

	@Override
	public void onObserved(CommonEventSource source) {
		AggregatorEventSource evt = (AggregatorEventSource)source;
        @SuppressWarnings("unchecked")
		List<Student> students = evt.getArg(1, List.class);
         
        for(Student student : students){
            student.setScore("100.00");
        }
         
        evt.putResult(this, students);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

}
