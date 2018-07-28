package eventcenter.api;

import java.io.Serializable;
import java.util.Date;

/**
 * When {@link EventListenerTask} executed, it would create a receipt for filter after executed
 * Created by liumingjian on 16/1/26.
 */
public class ListenerReceipt implements Serializable {

    private static final long serialVersionUID = -7526153077447243867L;

    /**
     * if listener executed success, it would calculate which listener consumed event time.
     */
    private Long took;

    /**
     * time of listener start
     */
    private Date start;

    /**
     * if listener executed failure, exception would be set.
     */
    private Throwable exception;

    /**
     * if listener executed success, it would be true.
     */
    private boolean success;

    /**
     * executed event listener
     */
    private transient EventListener eventListener;

    /**
     * executed event source
     */
    private EventSourceBase evt;

    /**
     * if listener executed success, it would calculate which listener consumed event time.
     * @return
     */
    public Long getTook() {
        return took;
    }

    /**
     * if listener executed success, it would calculate which listener consumed event time.
     * @param took
     */
    public void setTook(Long took) {
        this.took = took;
    }

    /**
     * if listener executed failure, exception would be set.
     * @return
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * if listener executed failure, exception would be set.
     * @param exception
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * if listener executed success, it would be true.
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * if listener executed success, it would be true.
     * @param success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * executed event listener
     * @return
     */
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * executed event listener
     * @param eventListener
     */
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * executed event source
     * @return
     */
    public EventSourceBase getEvt() {
        return evt;
    }

    /**
     * executed event source
     * @param evt
     */
    public void setEvt(EventSourceBase evt) {
        this.evt = evt;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public static ListenerReceipt buildSuc(EventListener listener, EventSourceBase evt, Long start, Long took){
        ListenerReceipt receipt = new ListenerReceipt();
        receipt.setEventListener(listener);
        receipt.setEvt(evt);
        receipt.setTook(took);
        receipt.setStart(new Date(start));
        receipt.setSuccess(true);
        return receipt;
    }

    public static ListenerReceipt buildFail(EventListener listener, EventSourceBase evt, Long start, Throwable e){
        ListenerReceipt receipt = new ListenerReceipt();
        receipt.setEventListener(listener);
        receipt.setEvt(evt);
        receipt.setStart(new Date(start));
        receipt.setException(e);
        receipt.setSuccess(false);
        return receipt;
    }
}
