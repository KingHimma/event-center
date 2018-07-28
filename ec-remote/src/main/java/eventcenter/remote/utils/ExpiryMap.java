package eventcenter.remote.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 具有有效期的Map，初始化之后，需要
 * @author JackyLIU
 *
 */
public class ExpiryMap<K, V> {

	private final Map<K, ExpiryValue<V>> map;
	
	/**
	 * 监控
	 */
	private Thread monitor;
	
	/**
	 * 监控间隔，默认为1秒
	 */
	private long checkInterval = 1;
	
	private volatile boolean start;
	
	private Object lock = new Object();
	
	private ExpiriedCallback<K,V> expiriedCallback;
	
	public long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public ExpiryMap(){
		this(new HashMap<K, ExpiryValue<V>>());
	}
	
	public ExpiryMap(int initialCapacity){
		this(new HashMap<K, ExpiryValue<V>>(initialCapacity));
	}
	
	public ExpiryMap(Map<K, ExpiryValue<V>> map){
		this.map = map;
	}
	
	public Map<K, ExpiryValue<V>> getMap() {
		return map;
	}
	
	public boolean containKey(K key){
		return map.containsKey(key);
	}
	
	public void put(K key, long expiry, V value){
		map.put(key, ExpiryValue.build(expiry, value));
	}
	
	public void put(K key, Date created, long expiry, V value){
		map.put(key, ExpiryValue.build(created, expiry, value));
	}
	
	public ExpiryValue<V> remove(K key){
		return map.remove(key);
	}
	
	public V getValue(K key){
		ExpiryValue<V> value = map.get(key);
		if(null == value)
			return null;
		
		return value.getValue();
	}
	
	public ExpiryValue<V> get(K key){
		return map.get(key);
	}

	public boolean isStart(){
		return start;
	}

	/**
	 * 使用之前先启动
	 */
	public void startup(){
		if(start)
			return ;
		
		if(checkInterval <= 0)
			throw new IllegalArgumentException("checkInterval has to be more than zero");
		
		start = true;
		monitor = new Thread(new Runnable(){
			@Override
			public void run() {
				while(start){					
					synchronized(lock){
						try {
							lock.wait(checkInterval * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					checkExpiry();
				}
			}
		});
		monitor.start();
	}
	
	protected void checkExpiry(){
		Set<K> keys = map.keySet();
		List<K> delete = new ArrayList<K>();
		for(K key : keys){
			ExpiryValue<V> value = map.get(key);
			if(isExpiried(value)){
				delete.add(key);
			}
		}
		
		for(K key : delete){
			ExpiryValue<V> value = map.remove(key);
			if(null == expiriedCallback)
				continue;
			try{
				expiriedCallback.onExpiried(key, value==null?null:value.getValue());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	protected boolean isExpiried(ExpiryValue<V> value){
		if(value == null || value.getCreated() == null || value.getExpiry() <= 0 )
			return true;

		Date expiryDate = new Date(value.getCreated().getTime() + value.getExpiry());
		return new Date().after(expiryDate);
	}
	
	/**
	 * 还应该调用shutdown进行关闭
	 */
	public void shutdown(){
		if(!start)
			return ;
		
		start = false;
		synchronized(lock){
			lock.notifyAll();
		}
	}

	public ExpiriedCallback<K,V> getExpiriedCallback() {
		return expiriedCallback;
	}

	public void setExpiriedCallback(ExpiriedCallback<K,V> expiriedCallback) {
		this.expiriedCallback = expiriedCallback;
	}
	
	@Override
	public String toString() {
		return map.toString();
	}

	public static class ExpiryValue<V> implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8422716038591883058L;

		/**
		 * 存放的时间
		 */
		private Date created;
		
		/**
		 * 有效期
		 */
		private long expiry;
		
		private V value;

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			this.created = created;
		}

		public long getExpiry() {
			return expiry;
		}

		public void setExpiry(long expiry) {
			this.expiry = expiry;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
		
		public static <V> ExpiryValue<V> build(long expiry, V value){
			return build(new Date(), expiry, value);
		}
		
		public static <V> ExpiryValue<V> build(Date created, long expiry, V value){
			ExpiryValue<V> ev = new ExpiryValue<V>();
			ev.setCreated(created);
			ev.setExpiry(expiry);
			ev.setValue(value);
			return ev;
		}
		
		@Override
		public String toString(){
			return new StringBuilder("{created:").append(created)
					.append(",expiry:").append(expiry)
					.append(",value:").append(value).append("}").toString();
		}
	}
	
	/**
	 * 当有效期过了之后，将会触发回调方法
	 * @author JackyLIU
	 *
	 */
	public static interface ExpiriedCallback<K,V>{
		
		public void onExpiried(K key, V value);
	}
}
