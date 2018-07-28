package eventcenter.aggr;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventInfo;
import eventcenter.api.EventListener;
import eventcenter.api.aggregator.EventAggregatable;
import eventcenter.api.aggregator.ListenersConsumedResult;
import eventcenter.api.aggregator.ResultAggregator;
import eventcenter.api.aggregator.support.ReferenceResultAggregator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 示例服务，里面的方法将会触发事件
 * @author JackyLIU
 *
 */
@Service
public class ExampleService {

	/**
	 * 手动触发事件时，需要引入此接口，如果使用Spring框架，可以使用@Resource标签注入进来
	 */
	@Resource
	private EventAggregatable eventCenter;

    public EventAggregatable getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventAggregatable eventCenter) {
        this.eventCenter = eventCenter;
    }

    /**
     * 使用aggregator listeners并发处理多个查询，Student有四个个属性，这里将会并发运行三个监听器，第一个是
     * 加密手机号码，第二个是设置默认的班级，第三个是计算期末考试总分的，然后将这个三个结果合并到一起返回出来
     * @param args
     * @return
     */
    public List<Student> query(String args){
        List<Student> students = queryStudents();
        return eventCenter.fireAggregateEvent(this, new EventInfo("students.query").setArgs(new Object[]{args, students}), new ResultAggregator<List<Student>>() {
             
            @Override
            public Object exceptionHandler(EventListener listener,
                                           CommonEventSource source, Exception e) {
                return null;
            }
            @SuppressWarnings("unchecked")
            @Override
            public List<Student> aggregate(ListenersConsumedResult results) {
                // 由于students已经在触发事件之前创建好了，每个监听器都只是修改student的参数而已，所以直接返回参数值即可
                return ((CommonEventSource)results.getSource()).getArg(1, List.class);
            }
        });
    }
     
    public List<Student> query4ReferrenceAggregatorResult(String args){
        List<Student> students = queryStudents();
        return eventCenter.fireAggregateEvent(this, new EventInfo("students.query").setArgs(new Object[]{args, students}), new ReferenceResultAggregator<List<Student>>(1));
    }
     
    private List<Student> queryStudents(){
        List<Student> students = new ArrayList<Student>();
        students.add(createStudent("Jacky LIU", "2班"));
        students.add(createStudent("Clark", "3班"));
        students.add(createStudent("Jerry", "4班"));
        students.add(createStudent("黄杰", null));
        students.add(createStudent("左韵", null));
        students.add(createStudent("章舟杰", null));
        students.add(createStudent("苏苏", null));
        students.add(createStudent("康康", null));
        students.add(createStudent("周杰", null));
        students.add(createStudent("双双", null));
        return students;
    }
     
    private Student createStudent(String name, String classroom){
        Student student = new Student();
        student.setName(name);
        student.setClassroom(classroom);
        return student;
    }
	
	
}
