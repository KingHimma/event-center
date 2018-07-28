package eventcenter.leveldb;

import java.io.Closeable;

/**
 * House Keeping 
 * @author JackyLIU
 *
 */
public interface HouseKeepingStrategy extends Closeable{

	/**
	 * open house keeping schedule
	 */
	void open();
}
