package eventcenter.monitor.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 16/3/8.
 */
public class IndexConfigs {

    private int total;

    private List<IndexConfig> list;

    public List<IndexConfig> getList() {
        if(null == list)
            list = new ArrayList<IndexConfig>();
        return list;
    }

    public void setList(List<IndexConfig> list) {
        this.list = list;
    }

    public int getTotal() {

        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public static IndexConfigs build(int total, List<IndexConfig> list){
        IndexConfigs configs = new IndexConfigs();
        configs.setTotal(total);
        configs.setList(list);
        return configs;
    }
}
