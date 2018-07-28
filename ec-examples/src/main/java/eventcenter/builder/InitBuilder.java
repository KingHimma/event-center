package eventcenter.builder;

import eventcenter.api.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class InitBuilder {

    public static List<EventListener> buildEventListeners(){
        List<EventListener> listeners = new ArrayList<EventListener>();
        listeners.add(new eventcenter.builder.listeners.AnnotationEventListener());
        listeners.add(new eventcenter.builder.listeners.AnnotationSyncEventListener());
        listeners.add(new eventcenter.builder.listeners.ManualEventListener());
        listeners.add(new eventcenter.builder.listeners.MultiSubscribEventListener());
        return listeners;
    }
}
