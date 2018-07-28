package eventcenter.monitor.server.controller;

import eventcenter.monitor.server.dao.IndexConfigCollection;
import eventcenter.monitor.server.model.IndexConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by liumingjian on 16/2/25.
 */
@Controller
@Scope("prototype")
@RequestMapping("/index/config")
public class IndexConfigController {

    @Resource
    IndexConfigCollection indexConfigCollection;

    public IndexConfigCollection getIndexConfigCollection() {
        return indexConfigCollection;
    }

    public void setIndexConfigCollection(IndexConfigCollection indexConfigCollection) {
        this.indexConfigCollection = indexConfigCollection;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody
    Object list(String group, Integer pageNo, Integer pageSize){
        return indexConfigCollection.search(group, pageNo, pageSize);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public @ResponseBody void save(IndexConfig config){
        indexConfigCollection.save(config);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody void delete(String id){
        indexConfigCollection.deleteById(id);
    }
}
