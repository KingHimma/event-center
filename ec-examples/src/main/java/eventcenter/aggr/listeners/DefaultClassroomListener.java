package eventcenter.aggr.listeners;

import eventcenter.aggr.Student;
import eventcenter.api.EventSourceBase;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.AggregatorEventSource;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 班级处理监听器
 * @author JackyLIU
 *
 */
@Component
@ListenerBind("students.query")
public class DefaultClassroomListener implements AggregatorEventListener {

	@Override
    @SuppressWarnings("unchecked")
    public void onObserved(EventSourceBase source) {
        AggregatorEventSource evt = (AggregatorEventSource)source;
        List<Student> students = evt.getArg(1, List.class);
        for(Student student : students){
            if(null == student.getClassroom()){
                student.setClassroom("1班");
            }
        }
         
        evt.putResult(this, students);
    }

}
