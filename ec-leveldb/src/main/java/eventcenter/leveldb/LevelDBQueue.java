package eventcenter.leveldb;

import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.MessageListener;
import eventcenter.api.async.QueueException;
import eventcenter.api.tx.EventTxnStatus;
import eventcenter.api.tx.ResumeTxnHandler;
import eventcenter.api.tx.TransactionalSupport;
import eventcenter.leveldb.strategy.LimitReadHouseKeepingStrategy;
import eventcenter.leveldb.tx.TransactionConfig;
import eventcenter.leveldb.tx.TxnQueueComponent;
import eventcenter.leveldb.tx.TxnQueueComponentFactory;
import eventcenter.leveldb.tx.UnopenTxnModeException;
import org.apache.log4j.Logger;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;

/**
 * use read cursor and write cursor to offer and transfer data
 * @author JackyLIU
 *
 */
public class LevelDBQueue implements EventQueue, TransactionalSupport {

	private final QueueMiddleComponent adapter;
	
	private volatile boolean isOpen = false;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private HouseKeepingStrategy houseKeepingStrategy;

	private final Object locker = new Object();

	/**
	 * 是否支持事务，设置了这个类表示需要开启事务模式
	 */
	private TransactionConfig transactionConfig;

	/**
	 * 当设置了 {@link #transactionConfig}之后，将会在 {@link #open()}的时候开启此控件
	 */
	private TxnQueueComponent txnQueueComponent;

	/**
	 * 事务容量数，设置了{@link #txnQueueComponent}才有效
	 */
	private Integer txnCapacity = 1000;
	
	public LevelDBQueue(QueueMiddleComponent adapter){
		this.adapter = adapter;
	}
	
	public void open() throws IOException{
		isOpen = true;
		this.adapter.open();
		openTxnQueueComponent();
		if(null == houseKeepingStrategy){
			// default create Limit ReadHouseKeeping
			houseKeepingStrategy = new LimitReadHouseKeepingStrategy(this);
		}
		houseKeepingStrategy.open();
	}

	public boolean isOpenTxnMode(){
		return null != transactionConfig;
	}

	private void openTxnQueueComponent(){
		if(!isOpenTxnMode())
			return ;
		txnQueueComponent = TxnQueueComponentFactory.create(adapter.getQueueName(), adapter.getDB(), transactionConfig, txnCapacity);
		try {
			txnQueueComponent.open();
			adapter.setOpenTxn(true);
		} catch (Exception e) {
			logger.error("startup txnQueueComponent failure:" + e.getMessage(), e);
		}
	}
	
	@Override
	public void close() throws IOException {
		if(null != txnQueueComponent){
			txnQueueComponent.shutdown();
		}
		this.adapter.close();
		this.houseKeepingStrategy.close();
		isOpen = false;
		synchronized(this){
			this.notifyAll();
		}
	}

	@Override
	public void offer(EventSourceBase evt) throws QueueException {
		try {
			adapter.save(evt);
			unlock();
		} catch (PersistenceException e) {
			throw new QueueException(e);
		}
	}

	@Override
	public void offer(EventSourceBase evt, long timeout) {
		offer(evt);
	}

	@Override
	public EventSourceBase transfer() {
		return transfer(-1L);
	}

	@Override
	public EventSourceBase transfer(long timeout) {
		if(!isOpen){
			return null;
		}
		
		EventSourceBase evt = pop();
		if(evt != null)
			return evt;

		lock(timeout);
		if(timeout <= 0)
			return transfer(timeout);

		return pop();
	}

	EventSourceBase transfer(long timeout, WriteBatch wb){
		if(!isOpen){
			return null;
		}

		EventSourceBase evt = pop(wb);
		if(evt != null)
			return evt;

		lock(timeout);
		if(timeout <= 0)
			return transfer(timeout, wb);

		return pop(wb);
	}

	private void lock(long timeout){
		final Object locker = this.locker;
		synchronized(locker){
			try {
				if(timeout <= 0){
					locker.wait();
				}else{
					locker.wait(timeout);
				}
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void unlock(){
		final Object locker = this.locker;
		synchronized(locker){
			locker.notifyAll();
		}
	}

	private EventSourceBase pop(){
		if(!isOpen)
			return null;
		try {
			EventSourceWrapper eventWrapper = adapter.pop();
			if(null == eventWrapper)
				return null;
			if(!isOpenTxnMode())
				return eventWrapper.getEvt();
			return eventWrapper;
		} catch (Throwable e) {
			logger.error("pop message error", e);
			return null;
		}
	}

	private EventSourceBase pop(WriteBatch wb){
		if(!isOpen)
			return null;
		try {
			EventSourceWrapper eventWrapper = adapter.pop(wb);
			if(null == eventWrapper)
				return null;
			if(!isOpenTxnMode())
				return eventWrapper.getEvt();
			return eventWrapper;
		} catch (Throwable e) {
			logger.error("pop message error", e);
			return null;
		}
	}

	@Override
	public int enqueueSize() {
		return (int)adapter.count();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void setMessageListener(MessageListener listener) {

	}

	@Override
	public void resumeTxn(ResumeTxnHandler handler) throws Exception {
		getTxnQueueComponent().resumeTxn(handler);
	}

	@Override
	public EventTxnStatus getTxnStatus(EventSourceBase event, String txnId, EventListener listener) throws Exception {
		if(!isOpenTxnMode())
			throw new UnopenTxnModeException();
		return getTxnQueueComponent().getTxnStatus(event.getEventId(), listener.getClass(), txnId);
	}

	public EventTxnStatus getTxnStatus(EventSourceBase event, String txnId, EventListener listener, WriteBatch writeBatch) throws Exception {
		if(!isOpenTxnMode())
			throw new UnopenTxnModeException();
		return getTxnQueueComponent().getTxnStatus(event.getEventId(), listener.getClass(), txnId, writeBatch);
	}

	@Override
	public void commit(EventTxnStatus txnStatus) throws Exception {
		if(!isOpenTxnMode())
			throw new UnopenTxnModeException();

		getTxnQueueComponent().commit(txnStatus);
	}

	public void houseKeeping() throws PersistenceException, IOException {
		getQueueMiddleComponent().houseKeeping();
		if(isOpenTxnMode()){
			getTxnQueueComponent().houseKeeping();
		}
	}

	public HouseKeepingStrategy getHouseKeepingStrategy() {
		return houseKeepingStrategy;
	}

	public void setHouseKeepingStrategy(HouseKeepingStrategy houseKeepingStrategy) {
		this.houseKeepingStrategy = houseKeepingStrategy;
	}

	public TransactionConfig getTransactionConfig() {
		return transactionConfig;
	}

	public void setTransactionConfig(TransactionConfig transactionConfig) {
		this.transactionConfig = transactionConfig;
	}

	public Integer getTxnCapacity() {
		return txnCapacity;
	}

	public void setTxnCapacity(Integer txnCapacity) {
		this.txnCapacity = txnCapacity;
	}

	public TxnQueueComponent getTxnQueueComponent() {
		return txnQueueComponent;
	}

	public QueueMiddleComponent getQueueMiddleComponent(){
		return adapter;
	}
}
