package eventcenter.leveldb;

import org.apache.log4j.Logger;


/**
 * 
 * @author JackyLIU
 *
 */
public abstract class AbstractHouseKeepingStrategy implements HouseKeepingStrategy {

	protected final LevelDBQueue queue;
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public AbstractHouseKeepingStrategy(LevelDBQueue queue){
		this.queue = queue;
	}
}
