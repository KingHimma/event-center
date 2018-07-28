package eventcenter.builder.dubbo;

import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import eventcenter.remote.EventTransmission;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class EventTransmissionReferenceConfig extends ReferenceConfig<EventTransmission> {
    private static final long serialVersionUID = -6865003658207742L;

    protected Integer checkHealthTimeout;

    public EventTransmissionReferenceConfig() {
        super();
    }

    public EventTransmissionReferenceConfig(Reference reference) {
        super(reference);
    }

    void load(){
        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>(2);
        MethodConfig m1 = new MethodConfig();
        m1.setName("asyncTransmission");
        m1.setAsync(true);
        m1.setReturn(false);
        methodConfigs.add(m1);
        MethodConfig m2 = new MethodConfig();
        m2.setName("checkHealth");
        m2.setTimeout(checkHealthTimeout == null ? 1000 : checkHealthTimeout);
        methodConfigs.add(m2);
        setMethods(methodConfigs);
        setInterface(EventTransmission.class);
    }

    EventTransmissionReferenceConfig checkHealthTimeout(Integer timeout){
        this.checkHealthTimeout = timeout;
        return this;
    }
}
