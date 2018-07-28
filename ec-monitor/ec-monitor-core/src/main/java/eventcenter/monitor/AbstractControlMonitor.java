package eventcenter.monitor;

import eventcenter.api.AbstractEventCenter;
import eventcenter.api.CommonEventSource;
import eventcenter.api.ListenerReceipt;
import eventcenter.api.appcache.IdentifyContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

/**
 * 抽象的监控器
 * Created by liumingjian on 16/2/15.
 */
public abstract class AbstractControlMonitor implements ControlMonitor {

    protected static final DelayInitLock delayInitLock = new DelayInitLock();

    protected InfoForward infoForward;

    protected InfoStorage infoStorage;

    protected NodeInfo nodeInfo;

    protected String nodeName;

    protected MonitorDataCodec monitorDataCodec;

    protected String monitorDataCodecClazz;

    @Autowired
    protected AbstractEventCenter eventCenter;

    /**
     * 是否需要保存事件的数据，主要是eventArgs和eventResult
     */
    protected boolean saveEventData = false;

    /**
     * 设置客户端的群组
     */
    protected String group;

    protected volatile boolean open = false;

    protected boolean uploadNodeInfoError = false;

    protected HeartBeat heartBeat;

    protected long heartbeatInterval = 10000;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    protected final Logger logger = Logger.getLogger(this.getClass());

    public InfoStorage getInfoStorage() {
        return infoStorage;
    }

    public void setInfoStorage(InfoStorage infoStorage) {
        this.infoStorage = infoStorage;
    }

    public InfoForward getInfoForward() {
        return infoForward;
    }

    public void setInfoForward(InfoForward infoForward) {
        this.infoForward = infoForward;
    }

    public MonitorDataCodec getMonitorDataCodec() {
        return monitorDataCodec;
    }

    public void setMonitorDataCodec(MonitorDataCodec monitorDataCodec) {
        this.monitorDataCodec = monitorDataCodec;
    }

    public String getMonitorDataCodecClazz() {
        return monitorDataCodecClazz;
    }

    public void setMonitorDataCodecClazz(String monitorDataCodecClazz) {
        this.monitorDataCodecClazz = monitorDataCodecClazz;
    }

    @PostConstruct
    @Override
    public void startup() {
        if(open || delayInitLock.isLock()){
            return ;
        }
        init();
        openHeartBeat();
    }

    protected void init(){
        if(open || delayInitLock.isLock()){
            return ;
        }
        if(null == infoStorage){
            infoStorage = loadInfoStorage();
            try {
                infoStorage.open();
            } catch (Exception e) {
                throw new MonitorStorageException(e);
            }
        }
        if(null == monitorDataCodec && null != monitorDataCodecClazz){
            try {
                monitorDataCodec = instanceCodec(monitorDataCodecClazz);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        nodeInfo = loadNodeInfo();
        open = true;
    }

    protected void openHeartBeat(){
        if(null != heartBeat)
            return ;

        if(null == infoForward)
            throw new IllegalArgumentException("please set infoForward for monitor");
        heartBeat = new HeartBeat();
        Thread heartBeatThread = new Thread(heartBeat, "ec-monitor-heartbeart");
        heartBeatThread.start();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            if(null != heartBeat)
                heartBeat.shutdown();
            infoStorage.close();
            heartBeat = null;
            infoForward = null;
        } catch (Exception e) {
            throw new MonitorStorageException(e);
        }
        open = false;
    }

    protected void assertInfoStorageNotNull(){
        if(null == infoStorage)
            throw new IllegalArgumentException("please init infoStorage when save event info for monitor");
    }

    protected MonitorDataCodec instanceCodec(String clazz) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clzInfo = Class.forName(clazz);
        Constructor<?> constructor = clzInfo.getConstructor();
        return (MonitorDataCodec)constructor.newInstance();
    }

    protected abstract InfoStorage loadInfoStorage();

    @Override
    public void saveEventInfo(MonitorEventInfo mei) {
        assertInfoStorageNotNull();
        infoStorage.pushEventInfo(mei);
    }

    protected NodeInfo loadNodeInfo(){
        NodeInfo info = queryNodeInfo(false);
        if(null == info){
            info = new NodeInfo();
            try {
                info.setId(IdentifyContext.getId());
            } catch (IOException e) {
                logger.error("get identify id error:" + e.getMessage());
                info.setId(UUID.randomUUID().toString());
            }
        }
        info.setGroup(group);
        InetAddress hostAddress = getHostAddress();
        info.setHost(hostAddress.getHostAddress());
        if(info.getName() == null){
            if(null == nodeName){
                info.setName(hostAddress.getHostName());
            }else{
                info.setName(new StringBuilder(hostAddress.getHostName()).append("_").append(nodeName).toString());
            }
        }
        info.setStart(new Date());
        info.setStat(1);
        return info;
    }

    @Override
    public NodeInfo queryNodeInfo(boolean includeDetail) {
        if(!includeDetail)
            return nodeInfo;
        nodeInfo.setCountOfQueueBuffer(countOfQueueBuffer());
        nodeInfo.setQueueSize(queueSize());
        nodeInfo.setCountOfLiveThread(countOfLiveThread());
        return nodeInfo;
    }

    protected InetAddress getHostAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if(inetAddress instanceof Inet4Address){
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Error when getting host ip address: <" + e.getMessage() + ">.");
        }
        return null;
    }

    @Override
    public void saveListenerReceipt(ListenerReceipt receipt) {
        NodeInfo nodeInfo = queryNodeInfo(false);
        if(null == nodeInfo){
            logger.error("can't query node info from controlMonitor");
            return ;
        }

        MonitorEventInfo monitorEventInfo = null;
        try {
            if(receipt.getEvt() instanceof CommonEventSource && saveEventData){
                CommonEventSource evt = (CommonEventSource)receipt.getEvt();
                Object eventArgs = codecObject(evt.getEventName(), evt.getArgs());
                Object eventResult = codecObject(evt.getEventName(), evt.getResult());
                monitorEventInfo = MonitorEventInfo.buildWith(receipt, nodeInfo, eventArgs ,eventResult, new Date());
            }else{
                monitorEventInfo = MonitorEventInfo.buildWith(receipt, nodeInfo, null ,null, new Date());
            }
        }catch(Throwable e){
            logger.error(new StringBuilder("build monitorEventInfo error:").append(e.getMessage()));
        }
        if(null != monitorEventInfo){
            saveEventInfo(monitorEventInfo);
        }
    }

    public Object codecObject(String eventName, Object v){
        if(null == monitorDataCodec)
            return v;

        try{
            return monitorDataCodec.codec(eventName, v);
        }catch(Exception e){
            logger.error("codec event args error:" + e.getMessage(), e);
            return v;
        }
    }

    @Override
    public int queueSize() {
        return eventCenter.getAsyncContainer().queueSize();
    }

    @Override
    public int countOfMaxConcurrent() {
        return eventCenter.getAsyncContainer().countOfMaxConcurrent();
    }

    @Override
    public int countOfLiveThread() {
        return eventCenter.getAsyncContainer().countOfLiveThread();
    }

    @Override
    public int countOfQueueBuffer() {
        return eventCenter.getAsyncContainer().countOfQueueBuffer();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public AbstractEventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(AbstractEventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

    public boolean isSaveEventData() {
        return saveEventData;
    }

    public void setSaveEventData(boolean saveEventData) {
        this.saveEventData = saveEventData;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    /**
     * 定时发送心跳数据包
     */
    public class HeartBeat implements Runnable {

        protected volatile boolean beatOpen = true;

        protected final Object lock = new Object();

        @Override
        public void run() {
            while(open && beatOpen){
                try{
                    infoForward.forwardNodeInfo(queryNodeInfo(true));   // 发送心跳的信息，需要收集容器的相关数据
                }catch(Exception e){
                    logger.error(new StringBuilder("send heartbeat error:").append(e.getMessage()), e);
                }

                sleep(heartbeatInterval);
            }
        }

        public void shutdown(){
            beatOpen = false;
            synchronized (lock){
                lock.notifyAll();
            }
        }

        protected void sleep(long sleepTime){
            synchronized(lock){
                try {
                    lock.wait(sleepTime);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 延迟加载锁，由于{@link AbstractControlMonitor}中的startup方法使用@PostConstruct注解，当使用混合的ControlMonitor时，加载ControlMonitor之前就已经初始化了，所以会导致一些必要的信息没有加载进来，
     * 需要使用这个延迟加载的锁进行控制
     */
    protected static class DelayInitLock {

        ThreadLocal<Integer> stat = new ThreadLocal<Integer>();

        public void lock(){
            stat.set(1);
        }

        public void unlock(){
            stat.remove();
        }

        public boolean isLock(){
            return stat.get() != null;
        }
    }
}
