package eventcenter.api;

/**
 *
 * Created by liumingjian on 16/1/26.
 */
public class ListenerFilterAdapter implements ListenerFilter {

    @Override
    public boolean before(EventListener listener, CommonEventSource evt) {
        return true;
    }

    @Override
    public void after(ListenerReceipt receipt) {

    }
}
