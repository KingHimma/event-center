package eventcenter.leveldb.tx;

import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.tx.EventTxnStatus;
import eventcenter.api.tx.ResumeTxnHandler;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liumingjian on 2016/12/23.
 */
public class TxnQueueComponent {

    private static final String KEY_INFO = "txn_queue_";

    private final String queueName;

    private final DB db;

    BucketGroup bucketGroup;

    private TxnQueueInfo txnQueueInfo;

    /**
     * 最大同时运行事务的数量，默认为10000
     */
    private Integer txnCapacity = 1000;

    /**
     * 最多可存储失败事务的容量，默认为10000
     */
    private Integer failureCapacity = 10000;

    /**
     * 最多可存储丢弃事务的容量，默认为100000
     */
    private Integer discardCapacity = 10000;

    /**
     * 重试事务的次数
     */
    private Integer retryCount = 3;

    /**
     * 事务超时时间
     */
    private Integer txnTimeout = 60;

    /**
     * 是否开启，超时或者事件消费异常的重试机制
     */
    private boolean openRetry = false;

    private volatile boolean isOpen = false;

    private String indexPrefix;

    private final ReentrantLock lock = new ReentrantLock();

    private final Logger logger = Logger.getLogger(this.getClass());

    public TxnQueueComponent(String queueName, DB db){
        this.db = db;
        this.queueName = queueName;
        bucketGroup = new BucketGroup();
        indexPrefix = buildTxnPrefix();
    }

    private String buildTxnQueueInfoKey(){
        return new StringBuilder(KEY_INFO).append(queueName).toString();
    }

    public void open() throws IOException {
        if(isOpen)
            return ;
        loadInfo();
        loadBuckets();
        isOpen = true;
    }

    /**
     * 当前未提交的事务数量，这个方法调用会消耗一定性能，调用过程中需要通过线程锁，保证获取的数据是最新的，所以尽可能的减少调用
     * @return
     */
    public int countOfTxn() throws Exception {
        return bucketGroup.txnBucket.count();
    }

    public void shutdown(){
        this.isOpen = false;
    }

    private void loadInfo() throws IOException {
        String infoKey = buildTxnQueueInfoKey();
        txnQueueInfo = LevelDBBucket.get(db, infoKey, TxnQueueInfo.class);
        if(null == txnQueueInfo){
            txnQueueInfo = new TxnQueueInfo();
        }
    }

    private void updateInfo() throws IOException {
        LevelDBBucket.set(db, buildTxnQueueInfoKey(), txnQueueInfo);
    }

    /**
     * 加载三个bucket内容
     */
    private void loadBuckets() throws IOException {
        boolean isFirstLoad = true;
        if(StringHelper.isNotEmpty(txnQueueInfo.getTxnBucketId())){
            isFirstLoad = false;
        }
        bucketGroup.loadTxnBucket(db, txnQueueInfo.getTxnBucketId());
        //bucketGroup.loadFailureBucket(db, txnQueueInfo.getFailureBucketId());
        //bucketGroup.loadDiscardBucket(db, txnQueueInfo.getDiscardBucketId());
        if(isFirstLoad){
            updateInfo();
        }
    }

    public Integer getTxnCapacity() {
        return txnCapacity;
    }

    public void setTxnCapacity(Integer txnCapacity) {
        this.txnCapacity = txnCapacity;
    }

    public Integer getFailureCapacity() {
        return failureCapacity;
    }

    public void setFailureCapacity(Integer failureCapacity) {
        if(failureCapacity < txnCapacity*5)
            throw new IllegalArgumentException("failureCapacity need be more or equal than txnCapacity * 5");
        this.failureCapacity = failureCapacity;
    }

    public Integer getDiscardCapacity() {
        return discardCapacity;
    }

    public void setDiscardCapacity(Integer discardCapacity) {
        if(discardCapacity < txnCapacity*5)
            throw new IllegalArgumentException("failureCapacity need be more or equal than txnCapacity * 5");
        this.discardCapacity = discardCapacity;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isOpenRetry() {
        return openRetry;
    }

    public void setOpenRetry(boolean openRetry) {
        this.openRetry = openRetry;
    }

    public void resumeTxn(final ResumeTxnHandler handler) throws IllegalAccessException, IOException {
        bucketGroup.resumeTxn(handler);
    }

    public EventTxnStatus getTxnStatus(String eventId, Class<? extends EventListener> listenerType, String txnId) throws Exception {
        return bucketGroup.getTxnStatus(eventId, listenerType, txnId, null);
    }

    public EventTxnStatus getTxnStatus(String eventId, Class<? extends EventListener> listenerType, String txnId, WriteBatch writeBatch) throws Exception {
        return bucketGroup.getTxnStatus(eventId, listenerType, txnId, writeBatch);
    }

    public void commit(EventTxnStatus txnStatus) throws Exception {
        bucketGroup.commit(txnStatus);
    }

    private void updateDB(WriteBatch wb) throws IOException {
        if(!isOpen)
            return ;
        db.write(wb);
        wb.close();
    }

    EventSourceBase getEvent(String txnId) throws IOException {
        return LevelDBBucket.get(db, wrapperKey(txnId), EventSourceBase.class);
    }

    private void deleteEvent(WriteBatch wb, String txnId){
        LevelDBBucket.delete(wb, wrapperKey(txnId));
        if(logger.isTraceEnabled()){
            logger.trace("delete event:" + txnId);
        }
    }

    String wrapperKey(String key){
        return new StringBuilder(queueName).append("_").append(key).toString();
    }

    String buildTxnRefKey(String txnId){
        return new StringBuilder(indexPrefix).append("_ref_").append(txnId).toString();
    }

    String buildTxnKey(String bucketTxnId){
        return new StringBuilder(indexPrefix).append("_").append(bucketTxnId).toString();
    }

    private String buildTxnPrefix(){
        return new StringBuilder("txn_").append(queueName).toString();
    }

    private String getBucketTxnIdFromIndex(String index){
        return index.substring(indexPrefix.length() + 1);
    }

    private <T> T lockAndInvoke(LockInvoker<T> invoker) throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try{
            if(!this.isOpen)
                throw new IllegalAccessException("queue had been closed");
            return invoker.lockAndInvoke();
        }finally {
            lock.unlock();
        }
    }

    public void houseKeeping() throws IOException {
        // Do nothing
        /*if(!this.isOpen)
            return ;

        final int batchCount = 10;
        boolean readOver = false;
        int deleteCount = 0;
        while(!readOver && isOpen){
            WriteBatch wb = db.createWriteBatch();
            try {
                List<String> indexList = bucketGroup.discardBucket.pop(wb, batchCount);
                if(indexList.size() == 0){
                    readOver = true;
                    break;
                }
                for(String index : indexList){
                    String txnId = getBucketTxnIdFromIndex(index);
                    deleteEvent(wb, txnId);
                    deleteStatus(wb, txnId);
                }
                deleteCount += indexList.size();
            }finally{
                updateDB(wb);
            }
        }
        if(logger.isDebugEnabled()){
            logger.debug(" delete event and txn count:" + deleteCount);
        }*/
    }

    class BucketGroup {
        /**
         * 具有事务特性的bucket，使用getTxn和commit将会操作这个txnBucket
         */
        LevelDBBucket txnBucket;

        /**
         * 当txnBucket中的事务超时，或者事务失败时，将会转移到这个bucket中
         */
        LevelDBBucket failureBucket;

        /**
         * 当事务被commit了，或者重试多次的事务未成功时，将会转移到这个bucket中，这个bucket将会由一个监控线程进行移出
         */
        LevelDBBucket discardBucket;

        /**
         * 是否调用过{@link #getTxnStatus(String, Class, String)}方法
         */
        private volatile boolean invokedGetTxnStatus = false;

        private void setInvokedGetTxnStatus(boolean invokedGetTxnStatus) {
            this.invokedGetTxnStatus = invokedGetTxnStatus;
        }

        private int calculateMaxPageCount(int elementCount){
            return elementCount/LevelDBBucket.DEFAULT_PAGE_CAPACITY + 1;
        }

        public void loadTxnBucket(DB db, String id) throws IOException {
            LevelDBBucket.Profile profile = new LevelDBBucket.Profile();
            profile.setOpenCache(true);
            profile.setMaxCountOfPage(calculateMaxPageCount(txnCapacity));
            txnBucket = buildBucket(db, profile, id);
            if(StringHelper.isEmpty(id)){
                txnQueueInfo.setTxnBucketId(txnBucket.getId());
            }
            if(logger.isDebugEnabled()){
                logger.debug("txnBucket's id:" + txnBucket.getId());
            }
        }

        public void loadFailureBucket(DB db, String id) throws IOException {
            LevelDBBucket.Profile profile = new LevelDBBucket.Profile();
            profile.setOpenCache(false);
            profile.setMaxCountOfPage(calculateMaxPageCount(failureCapacity));
            failureBucket = buildBucket(db, profile, id);
            if(StringHelper.isEmpty(id)){
                txnQueueInfo.setFailureBucketId(failureBucket.getId());
            }
            if(logger.isDebugEnabled()){
                logger.debug("failureBucket's id:" + txnBucket.getId());
            }
        }

        public void loadDiscardBucket(DB db, String id) throws IOException {
            LevelDBBucket.Profile profile = new LevelDBBucket.Profile();
            profile.setOpenCache(false);
            profile.setMaxCountOfPage(calculateMaxPageCount(discardCapacity));
            discardBucket = buildBucket(db, profile, id);
            if(StringHelper.isEmpty(id)){
                txnQueueInfo.setDiscardBucketId(discardBucket.getId());
            }
            if(logger.isDebugEnabled()){
                logger.debug("discardBucket's id:" + txnBucket.getId());
            }
        }

        private LevelDBBucket buildBucket(DB db, LevelDBBucket.Profile profile, String id) throws IOException {
            LevelDBBucket bucket = new LevelDBBucket.Builder(db).id(id).queueName(queueName)
                    .maxCountOfPage(profile.getMaxCountOfPage()).openCache(profile.isOpenCache())
                    .build();
            return bucket;
        }

        LevelDBEventTxnStatus createTxnStatus(String eventId, Class<? extends EventListener> listenerType, String txnId) throws Exception {
            WriteBatch writeBatch = db.createWriteBatch();
            try{
                return createTxnStatus(eventId, listenerType, txnId, writeBatch);
            }finally {
                updateDB(writeBatch);
            }
        }

        LevelDBEventTxnStatus createTxnStatus(String eventId, Class<? extends EventListener> listenerType, String txnId, final WriteBatch writeBatch) throws Exception {
            final LevelDBEventTxnStatus status = new LevelDBEventTxnStatus();
            status.setBucketId(txnQueueInfo.getTxnBucketId());
            status.setComplete(false);
            status.setStart(new Date());
            status.setTxnId(txnId);
            status.setBucketTxnId(UUID.randomUUID().toString());
            status.setEventId(eventId);
            status.setListenerType(listenerType);
            return lockAndInvoke(new LockInvoker<LevelDBEventTxnStatus>() {
                @Override
                public LevelDBEventTxnStatus lockAndInvoke() throws Exception {
                    final String index = buildTxnKey(status.getBucketTxnId());
                    status.setPageNo(txnBucket.savePageIndex(writeBatch, index));
                    saveStatus(writeBatch, status);
                    TxnRef txnRef = queryTxnRef(status.getTxnId());
                    if(null == txnRef){
                        txnRef = new TxnRef();
                        txnRef.setTxnId(status.getTxnId());
                    }
                    txnRef.getBucketTxnIds().add(status.getBucketTxnId());
                    txnRef.increaseTxnCount();
                    saveTxnRef(writeBatch, txnRef);
                    if(logger.isTraceEnabled()){
                        logger.trace(new StringBuilder("create event txn:").append(index).append(" for ").append(status.getListenerType().getName()).append(" success"));
                    }
                    return status;
                }
            });
        }

        private void saveStatus(final WriteBatch wb, final LevelDBEventTxnStatus status) throws Exception {
            lockAndInvoke(new LockInvoker<Void>() {
                @Override
                public Void lockAndInvoke() throws Exception {
                    LevelDBBucket.set(wb, buildTxnKey(status.getBucketTxnId()), status);
                    return null;
                }
            });
        }

        private void saveTxnRef(final WriteBatch wb, final TxnRef txnRef) throws Exception {
            lockAndInvoke(new LockInvoker<Void>() {
                @Override
                public Void lockAndInvoke() throws Exception {
                    LevelDBBucket.set(wb, buildTxnRefKey(txnRef.getTxnId()), txnRef);
                    return null;
                }
            });
        }

        private TxnRef queryTxnRef(String txnId) throws IOException {
            return LevelDBBucket.get(db, buildTxnRefKey(txnId), TxnRef.class);
        }

        EventTxnStatus getTxnStatus(String eventId, Class<? extends EventListener> listenerType, String txnId, WriteBatch writeBatch) throws Exception {
            LevelDBEventTxnStatus status = findStatus(txnId, listenerType);
            if(null != status)
                return status;
            if(!invokedGetTxnStatus)
                setInvokedGetTxnStatus(true);
            if(null != writeBatch)
                return createTxnStatus(eventId, listenerType, txnId, writeBatch);
            return createTxnStatus(eventId, listenerType, txnId);
        }

        private void removeTxnBucketIndex(String index) throws IOException, IllegalAccessException {
            WriteBatch wb = db.createWriteBatch();
            try {
                txnBucket.popByIndex(wb, index);
                db.write(wb);
            }finally{
                wb.close();
            }
        }

        void commit(EventTxnStatus txnStatus) throws Exception {
            final LevelDBEventTxnStatus status = (LevelDBEventTxnStatus)txnStatus;
            lockAndInvoke(new LockInvoker<Void>() {
                @Override
                public Void lockAndInvoke() throws Exception {
                    WriteBatch writeBatch = db.createWriteBatch();
                    // begin to move index from txnBucket to discardBucket
                    try {
                        String index = buildTxnKey(status.getBucketTxnId());
                        index = txnBucket.popByIndex(writeBatch, index);
                        if (null == index)
                            throw new IllegalAccessException("txn can't be found or may had been commit!");
                        if (deleteStatus(writeBatch, status)) {
                            deleteEvent(writeBatch, status.getTxnId());
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace(new StringBuilder("commit event txn:").append(status.getTxnId()).append(" for ").append(status.getListenerType().getName()).append(" success"));
                        }
                    } finally {
                        updateDB(writeBatch);
                    }
                    return null;
                }
            });
        }

        void resumeTxn(final ResumeTxnHandler handler) throws IllegalAccessException, IOException {
            if(invokedGetTxnStatus)
                throw new IllegalAccessException("it must be invoked before first getTxnStatus");
            if(null == handler)
                throw new IllegalArgumentException("please set ResumeTxnHandler parameter");

            txnBucket.iterateIndex(new IndexIterator() {
                @Override
                public void iterateIndex(String index, int pageNo) throws Exception {
                    String bucketTxnId = getBucketTxnIdFromIndex(index);
                    LevelDBEventTxnStatus txn = findStatusByBucketTxnId(bucketTxnId);
                    if (null == txn) {
                        logger.warn("can't find txn[" + bucketTxnId + "]");
                        removeTxnBucketIndex(index);
                        return;
                    }
                    EventSourceBase event = null;
                    boolean fail2delete = false;
                    try {
                        event = getEvent(txn.getTxnId());
                    }catch(Exception e){
                        logger.error("getEvent error, then it would direct to delete event:" + e.getMessage(), e);
                        fail2delete = true;
                    }
                    if (null == event) {
                        logger.warn("can't find event[" + bucketTxnId + "]");
                        WriteBatch wb = db.createWriteBatch();
                        try {
                            txnBucket.popByIndex(wb, index);
                            deleteStatus(wb, txn);
                            if(fail2delete){
                                deleteEvent(wb, txn.getTxnId());
                            }
                            db.write(wb);
                        } finally {
                            wb.close();
                        }
                        return;
                    }
                    handler.resume(txn, event);
                }
            });
        }

        /**
         * 如果都将txnRef中的txnIds删除了，那么返回true
         * @param wb
         * @param status
         * @return
         * @throws IOException
         */
        private boolean deleteStatus(final WriteBatch wb, final LevelDBEventTxnStatus status) throws Exception {
            return lockAndInvoke(new LockInvoker<Boolean>() {
                @Override
                public Boolean lockAndInvoke() throws Exception {

                    LevelDBBucket.delete(wb, buildTxnKey(status.getBucketTxnId()));
                    TxnRef txnRef = queryTxnRef(status.getTxnId());
                    if(null == txnRef){
                        return true;
                    }
                    txnRef.decreaseTxnCount();
                    txnRef.getBucketTxnIds().remove(status.getBucketTxnId());
                    return txnRef.getTxnCount() <= 0;
                }
            });
        }

        LevelDBEventTxnStatus findStatus(String txnId, Class<? extends EventListener> listenerType) throws IOException {
            TxnRef txnRef = queryTxnRef(txnId);
            if(null == txnRef)
                return null;
            List<LevelDBEventTxnStatus> list = new ArrayList<LevelDBEventTxnStatus>(txnRef.getTxnCount());
            for(String bucketTxnId : txnRef.getBucketTxnIds()){
                LevelDBEventTxnStatus status = findStatusByBucketTxnId(bucketTxnId);
                if(status.getListenerType() == listenerType)
                    return status;
            }
            return null;
        }

        LevelDBEventTxnStatus findStatusByBucketTxnId(String bucketTxnId) throws IOException {
            return LevelDBBucket.get(db, buildTxnKey(bucketTxnId), LevelDBEventTxnStatus.class);
        }
    }
}
