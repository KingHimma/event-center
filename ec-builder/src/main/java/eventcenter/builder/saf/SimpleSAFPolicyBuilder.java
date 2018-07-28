package eventcenter.builder.saf;

import eventcenter.remote.saf.simple.SimpleStoreAndForwardPolicy;

/**
 * 构建{@link eventcenter.remote.saf.simple.SimpleStoreAndForwardPolicy}实例的构建器
 * @author liumingjian
 * @date 2018/5/3
 **/
public class SimpleSAFPolicyBuilder {

    SimpleStoreAndForwardPolicy policy = new SimpleStoreAndForwardPolicy();

    public SimpleSAFPolicyBuilder storeOnSendFail(boolean isStoreOnSendFail){
        policy.setStoreOnSendFail(isStoreOnSendFail);
        return this;
    }

    public SimpleSAFPolicyBuilder queueCapacity(int queueCapacity){
        policy.setQueueCapacity(queueCapacity);
        return this;
    }

    public SimpleSAFPolicyBuilder checkInterval(long checkInterval){
        policy.setCheckInterval(checkInterval);
        return this;
    }

    public SimpleStoreAndForwardPolicy build(){
        return policy;
    }
}
