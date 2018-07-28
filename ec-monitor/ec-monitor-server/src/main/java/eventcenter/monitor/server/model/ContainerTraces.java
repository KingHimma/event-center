package eventcenter.monitor.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 16/5/31.
 */
public class ContainerTraces implements Serializable {
    private static final long serialVersionUID = -5713337228485872142L;

    private int total;

    private List<ContainerTrace> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ContainerTrace> getList() {
        if(null == list)
            list = new ArrayList<ContainerTrace>();
        return list;
    }

    public void setList(List<ContainerTrace> list) {
        this.list = list;
    }
}
