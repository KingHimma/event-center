package eventcenter.api;

import java.util.List;


/**
 * 通用型的事件源，如果需要省去{@link EventRegister}的实现，可以直接使用这个Source， 这个Source会按照事件的点的参数传递进来，例如
 * 方法调用的参数，结果数据，都会包装进来
 * @author JackyLIU
 *
 */
public class CommonEventSource extends EventSourceBase {

	private Object[] args;
	
	private Object result;
	
	public CommonEventSource(Object source, String eventId, String eventName, Object[] args, Object result, String mdcValue) {
		super(source, eventId, eventName,mdcValue);
		this.args = args;
		this.result = result;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4627790680230688987L;

	public Object[] getArgs() {
		return args;
	}

	public Object getResult() {
		return result;
	}

	/**
	 * 按照指定类型，获取参数，推荐使用这个方法获取参数，如果index下标超过了参数数组的长度，那么将会返回null。
	 * 如果指定的类型和输入的类型不一致，将会抛出
	 * @param index
	 * @param type
	 * @return@throws ClassCastException if the object is not
     * null and is not assignable to the type T.
	 */
	public <T> T getArg(int index, Class<T> type){
		if(args == null) {
			return null;
		}
		if(args.length <= index) {
			return null;
		}
		return type.cast(args[index]);
	}
	
	/**
	 * 按照指定类型，获取List参数
	 * @param index
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getArgList(int index, Class<T> type){
		if(args == null)
			return null;
		if(args.length <= index)
			return null;
		return (List<T>)args[index];		
	}
	
	/**
	 * 根据指定类型，返回结果数据
	 * @param type
	 * @return
	 * @return@throws ClassCastException if the object is not
     * null and is not assignable to the type T.
	 */
	public <T> T getResult(Class<T> type){
		if(null == result) {
			return null;
		}
		return type.cast(result);
	}
	
	/**
	 * 将新增的参数压入到数组最后一个元素里
	 * @param arg
	 */
	public void pushArg(Object arg){
		if(null == args){
			args = new Object[]{arg};
			return ;
		}
		
		Object[] nArgs = new Object[args.length + 1];
		System.arraycopy(args, 0, nArgs, 0, args.length);
		nArgs[nArgs.length - 1] = arg;
		args = nArgs;
	}
}
