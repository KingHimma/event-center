package eventcenter.monitor.server.controller;

import com.alibaba.fastjson.JSONObject;
import eventcenter.monitor.NodeInfo;
import eventcenter.monitor.server.dao.EventInfoCollection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by liumingjian on 16/2/25.
 */
@Controller
@Scope("prototype")
@RequestMapping("/event")
public class EventInfoController {

    @Resource
    EventInfoCollection eventInfoCollection;

    public EventInfoCollection getEventInfoCollection() {
        return eventInfoCollection;
    }

    public void setEventInfoCollection(EventInfoCollection eventInfoCollection) {
        this.eventInfoCollection = eventInfoCollection;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody
    Object list(String eventId, String eventName, Date start, Date end, String listenerClass, String mdcValue, String content, Integer pageNo, Integer pageSize, String nodeId, String nodeName, String nodeGroup){
        NodeInfo nodeInfo = null;
        if(!StringUtils.isEmpty(nodeId) || !StringUtils.isEmpty(nodeName) || !StringUtils.isEmpty(nodeGroup)){
            nodeInfo = new NodeInfo();
            nodeInfo.setId(nodeId);
            nodeInfo.setName(nodeName);
            nodeInfo.setGroup(nodeGroup);
        }
        return eventInfoCollection.search(eventId, eventName, start, end, listenerClass, mdcValue, content, pageNo, pageSize,nodeInfo);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public @ResponseBody
    Object detail(String id){
        return eventInfoCollection.queryById(id);
    }

    @RequestMapping(value = "/housekeeping", method = RequestMethod.POST)
    public @ResponseBody
    Object houseKeeping(Integer keepdays){
        if(null == keepdays)
            keepdays = 7;   //  默认保留7天
        eventInfoCollection.houseKeeping(keepdays);
        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        return jo;
    }
}
