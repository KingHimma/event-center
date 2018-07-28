package eventcenter.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 16/2/28.
 */
public class Trade implements Serializable {

    private String id;

    private String payment;

    private String tid;

    private String sellerNick;

    private String buyerNick;

    private String outSid;

    private String status;

    private List<Order> orders;

    public List<Order> getOrders() {
        if(null == orders)
            orders = new ArrayList<Order>();
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getSellerNick() {
        return sellerNick;
    }

    public void setSellerNick(String sellerNick) {
        this.sellerNick = sellerNick;
    }

    public String getBuyerNick() {
        return buyerNick;
    }

    public void setBuyerNick(String buyerNick) {
        this.buyerNick = buyerNick;
    }

    public String getOutSid() {
        return outSid;
    }

    public void setOutSid(String outSid) {
        this.outSid = outSid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
