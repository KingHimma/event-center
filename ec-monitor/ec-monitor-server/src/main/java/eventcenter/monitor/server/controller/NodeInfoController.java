package eventcenter.monitor.server.controller;

import eventcenter.monitor.server.dao.NodeInfoCollection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by liumingjian on 16/2/25.
 */
@Controller
@Scope("prototype")
@RequestMapping("/node")
public class NodeInfoController {

    @Resource
    NodeInfoCollection nodeInfoCollection;

    public NodeInfoCollection getNodeInfoCollection() {
        return nodeInfoCollection;
    }

    public void setNodeInfoCollection(NodeInfoCollection nodeInfoCollection) {
        this.nodeInfoCollection = nodeInfoCollection;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody
    Object list(String id, String group, String name, String host){
        return nodeInfoCollection.search(id,group,name,host);
    }
}
