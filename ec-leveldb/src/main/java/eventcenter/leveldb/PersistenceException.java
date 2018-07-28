package eventcenter.leveldb;

/**
 * 持久化异常
 * @author JackyLIU
 *
 */
public class PersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6108849590042130844L;

	public PersistenceException(String msg){
		super(msg);
	}
	
	public PersistenceException(Exception e){
		super(e);
	}
}
