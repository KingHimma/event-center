package eventcenter.api;

/**
 * After {@link AbstractEventCenter} startup, it would init context, set {@link EventCenterConfig} into context
 * Created by liumingjian on 16/1/26.
 */
public class ConfigContext {

    private static EventCenterConfig config;

    static void setEventCenterConfig(EventCenterConfig c){
        config = c;
    }

    public static EventCenterConfig getConfig(){
        if(null == config)
            throw new IllegalArgumentException("please startup event center first.");
        return config;
    }
}
