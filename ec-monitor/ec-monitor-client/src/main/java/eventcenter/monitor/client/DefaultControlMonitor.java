package eventcenter.monitor.client;

import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.InfoForward;
import eventcenter.monitor.InfoStorage;
import eventcenter.monitor.MonitorEventInfo;
import eventcenter.monitor.client.leveldb.LeveldbInfoStorage;
import eventcenter.monitor.AbstractControlMonitor;
import eventcenter.monitor.client.leveldb.LeveldbInfoStorage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.List;

/**
 * 默认的监控器实现
 * Created by liumingjian on 16/2/15.
 */
public class DefaultControlMonitor extends AbstractControlMonitor {

    protected long checkInterval = 10000;

    protected long nextPushInterval = 1000;

    /**
     * 批量推送监控事件的数据
     */
    protected int batchForwardSize = 10;

    /**
     * 数据缓存的文件路径
     */
    protected String dirPath;

    protected WatchDog watchDog;

    @Override
    protected InfoStorage loadInfoStorage() {
        LeveldbInfoStorage storage = new LeveldbInfoStorage();
        if(null != dirPath){
            storage.setDirPath(new File(new StringBuilder(dirPath).append(File.separator).append(".ecmonitor").toString()));
        }
        return storage;
    }

    /**
     *
     */
    public DefaultControlMonitor(){

    }

    @PostConstruct
    @Override
    public void startup() {
        super.startup();
        watchDog = new WatchDog(infoForward);
        Thread t = new Thread(watchDog, "ec-monitor-event-watchdog");
        t.start();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        super.shutdown();
        watchDog.shutdown();
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public long getNextPushInterval() {
        return nextPushInterval;
    }

    public void setNextPushInterval(long nextPushInterval) {
        this.nextPushInterval = nextPushInterval;
    }

    public int getBatchForwardSize() {
        return batchForwardSize;
    }

    public void setBatchForwardSize(int batchForwardSize) {
        this.batchForwardSize = batchForwardSize;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public class WatchDog implements Runnable{

        protected final InfoForward infoForward;

        /**
         * 上一次发送失败的元素
         */
        protected List<MonitorEventInfo> lastFailInfos;

        protected volatile boolean watchOpen = true;

        protected final Object lock = new Object();

        public WatchDog(InfoForward infoForward){
            this.infoForward = infoForward;
        }

        @Override
        public void run() {
            while(open && watchOpen){
                try{
                    executing();
                }catch(Throwable e){
                    logger.error(new StringBuilder("infoForward forward monitor info failure:").append(e.getMessage()));
                }

                sleep(checkInterval);
            }
        }

        void executing() throws Throwable {
            // 先判断之前发送的元素存在，存在表示上一次发送失败了，那么将重试上一次的元素
            if(null != lastFailInfos){
                forward(lastFailInfos);
                lastFailInfos = null;
            }

            lastFailInfos = infoStorage.popEventInfos(batchForwardSize);
            while(open && watchOpen){
                if(lastFailInfos.size() == 0){
                    sleep(nextPushInterval);
                }else{
                    forward(lastFailInfos);
                }
                lastFailInfos = infoStorage.popEventInfos(batchForwardSize);
            }
            lastFailInfos = null;
        }

        void forward(List<MonitorEventInfo> infos) throws Throwable {
            try {
                infoForward.forwardEventInfo(infos);
            }catch(Throwable e){
                lastFailInfos = infos;
                throw e;
            }
        }

        public void shutdown(){
            watchOpen = false;
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
}
