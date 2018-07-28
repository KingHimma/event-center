package eventcenter.monitor.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 16/3/8.
 */
public class MonitorEventInfoModels {

    private int total;

    private List<MonitorEventInfoModel> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<MonitorEventInfoModel> getList() {
        if(null == list)
            list = new ArrayList<MonitorEventInfoModel>();
        return list;
    }

    public void setList(List<MonitorEventInfoModel> list) {
        this.list = list;
    }

    public static MonitorEventInfoModels build(int total, List<MonitorEventInfoModel> list){
        MonitorEventInfoModels models = new MonitorEventInfoModels();
        models.setTotal(total);
        models.setList(list);
        return models;
    }
}
