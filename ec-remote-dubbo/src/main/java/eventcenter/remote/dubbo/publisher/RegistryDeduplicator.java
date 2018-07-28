package eventcenter.remote.dubbo.publisher;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 订阅ZK的通知的去重器，目前的问题是dubbo的registryService.subscribe(eventSubscriberUrl, createNotifyListener());方法之后
 * 监听器会在同一时间获取到至少两个订阅的通知，导致性能上的浪费，所以由这个来进行过滤
 * @author liumingjian
 * @date 2018/5/24
 **/
public class RegistryDeduplicator {

    /**
     * 暂存时间，如果两次同样的URL调用，那么判断前一次和这一次的调用时间差，如果小于holdTime，则方法{@link #isDeduplicate(URL)}返回true
     */
    private long holdTime = 10000L;

    private Map<URL, Long> cache = Collections.synchronizedMap(new HashMap<URL, Long>());

    private boolean isEmptyProtocol(URL url){
        return Constants.EMPTY_PROTOCOL.equals(url.getProtocol());
    }

    private URL buildProviderURL(URL url){
        URL copied = URL.valueOf(url.toFullString());
        copied.setProtocol(Constants.DUBBO_VERSION_KEY);
        return copied;
    }

    /**
     * 这个方法判断内部的缓存是否持有过url，如果持有过，则根据holdTime的时间，计算出这一次和上一次的时间差，
     * 如果在时间差内，则返回true，并将最新时间更新到缓存中；如果不在，则返回false，并记录当前时间到缓存中
     * @param url
     * @return
     */
    public boolean isDeduplicate(URL url){
        try {
            if (cache.containsKey(url)) {
                // 计算时间差
                final long interval = System.currentTimeMillis() - cache.get(url);
                if (interval <= holdTime) {
                    if(isEmptyProtocol(url)){
                        cache.remove(buildProviderURL(url));
                    }
                    return true;
                }
            }
            return false;
        }finally{
            cache.put(url, System.currentTimeMillis());
        }
    }

    public long getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(long holdTime) {
        this.holdTime = holdTime;
    }
}
