package eventcenter.builder.local;

import eventcenter.aggr.Student;
import eventcenter.aggr.listeners.DefaultClassroomListener;
import eventcenter.aggr.listeners.EncodeMobileListener;
import eventcenter.aggr.listeners.ScoreListener;
import eventcenter.aggr.listeners.SplitListener;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.AggregatorContainerBuilder;
import eventcenter.builder.EventCenterBuilder;

import java.util.List;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class MultiAggrMain {

    public static void main(String[] args) throws Exception {
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListener(new DefaultClassroomListener())
                .addEventListener(new EncodeMobileListener())
                .addEventListener(new ScoreListener())
                .addEventListener(new SplitListener())
                .aggregatorContainer(new AggregatorContainerBuilder()
                        .threadPoolInfo("students.query", 10, 20)
                        .simpleAggregatorContainer(10, 20).build()).build();
        eventCenter.startup();
        eventcenter.aggr.ExampleService es = new eventcenter.aggr.ExampleService();
        es.setEventCenter(eventCenter);
        List<Student> students = es.query("test");
        System.out.println(students);
        eventCenter.shutdown();
    }
}
