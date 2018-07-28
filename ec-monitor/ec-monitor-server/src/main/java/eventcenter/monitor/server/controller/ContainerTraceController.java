package eventcenter.monitor.server.controller;

import com.alibaba.fastjson.JSONObject;
import eventcenter.monitor.server.dao.EventContainerTraceCollection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 事件节点的统计数据
 * Created by liumingjian on 16/6/3.
 */
@Controller
@Scope("prototype")
@RequestMapping("/trace")
public class ContainerTraceController {

    @Resource
    EventContainerTraceCollection containerTraceCollection;

    public EventContainerTraceCollection getContainerTraceCollection() {
        return containerTraceCollection;
    }

    public void setContainerTraceCollection(EventContainerTraceCollection containerTraceCollection) {
        this.containerTraceCollection = containerTraceCollection;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody Object list(String nodeId, Integer pageNo, Integer pageSize, Long start, Long end){
        return containerTraceCollection.searchWithPage(nodeId, pageNo, pageSize, start != null?new Date(start):null, end != null?new Date(end):null);
    }

    @RequestMapping(value = "/housekeeping", method = RequestMethod.POST)
    public @ResponseBody
    Object houseKeeping(Integer keepdays){
        if(null == keepdays)
            keepdays = 7;   //  默认保留7天
        containerTraceCollection.houseKeeping(keepdays);
        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        return jo;
    }
}
