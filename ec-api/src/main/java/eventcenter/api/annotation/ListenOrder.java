package eventcenter.api.annotation;

/**
 * 监控事件的顺序，在方法之前监控还是在方法之后监控，还是两个顺序点都需要
 * @author JackyLIU
 *
 */
public enum ListenOrder {

	/**
	 * 在监控点之前，发出事件
	 */
	Before,
	
	/**
	 * 在监控点之后，发出事件
	 */
	After
}
