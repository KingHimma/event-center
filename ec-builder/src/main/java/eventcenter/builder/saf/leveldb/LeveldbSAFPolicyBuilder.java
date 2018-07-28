package eventcenter.builder.saf.leveldb;

import eventcenter.leveldb.saf.LevelDBStoreAndForwardPolicy;

/**
 * @author liumingjian
 * @date 2018/5/3
 **/
public class LeveldbSAFPolicyBuilder {

    LevelDBStoreAndForwardPolicy policy = new LevelDBStoreAndForwardPolicy();

    public LeveldbSAFPolicyBuilder storeOnSendFail(boolean isStoreOnSendFail){
        policy.setStoreOnSendFail(isStoreOnSendFail);
        return this;
    }

    public LeveldbSAFPolicyBuilder path(String path){
        policy.setPath(path);
        return this;
    }

    public LeveldbSAFPolicyBuilder checkInterval(long checkInterval){
        policy.setCheckInterval(checkInterval);
        return this;
    }

    public LeveldbSAFPolicyBuilder readLimitSize(int readLimitSize){
        policy.setReadLimitSize(readLimitSize);
        return this;
    }

    public LeveldbSAFPolicyBuilder levelDBName(String levelDBName){
        policy.setLevelDBName(levelDBName);
        return this;
    }

    public LeveldbSAFPolicyBuilder houseKeepingInterval(long houseKeepingInterval){
        policy.setHouseKeepingInterval(houseKeepingInterval);
        return this;
    }

    public LevelDBStoreAndForwardPolicy build(){
        return policy;
    }
}
