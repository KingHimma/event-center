package eventcenter.api.aggregator;

/**
 * 并发聚合运行异常
 * @author JackyLIU
 *
 */
public class AggregatorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3025926789623174479L;

	public AggregatorException(Exception e){
		super(e);
	}
	
	public AggregatorException(String msg){
		super(msg);
	}
}
