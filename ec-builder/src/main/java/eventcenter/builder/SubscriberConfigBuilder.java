package eventcenter.builder;

/**
 * Created by liumingjian on 2017/9/6.
 */
public class SubscriberConfigBuilder {

    protected SubscriberConfig subscriberConfig = createSubscriberConfig();

    protected SubscriberConfig createSubscriberConfig(){
        return new SubscriberConfig();
    }

    public SubscriberConfig build() {
        return subscriberConfig;
    }
}
