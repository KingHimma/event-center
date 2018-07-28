package eventcenter.leveldb.tx;

import eventcenter.leveldb.LevelDBPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * if {@link LevelDBBucket#isOpenCache()} is true, it would create cache for bucket for approving read and write capacity
 * Created by liumingjian on 2016/12/28.
 */
class BucketPageCache implements Serializable{

    final List<LevelDBPage> pages;

    final Map<String, Integer> indexMap = new HashMap<String, Integer>();

    public BucketPageCache(List<LevelDBPage> pages){
        this.pages = pages;
        init();
    }

    public BucketPageCache(int capacity){
        this.pages = new ArrayList<LevelDBPage>(capacity);
    }

    private void init(){
        for(LevelDBPage page : pages){
            fillIndexMap(page);
        }
    }

    private void fillIndexMap(LevelDBPage page){
        if(page.getIndexes() == null)
            return ;
        for(String index : page.getIndexes()){
            indexMap.put(index, (int) page.getNo());
        }
    }

    public void addPage(LevelDBPage page){
        this.pages.add(page);
        fillIndexMap(page);
    }

    public LevelDBPage getPageByNo(int pageNo) {
        try {
            return pages.get(pageNo - 1);
        }catch(IndexOutOfBoundsException e){
            return null;
        }
    }

    public LevelDBPage removeIndex(String index){
        Integer pageNo = indexMap.remove(index);
        if(null == pageNo)
            return null;
        LevelDBPage page = getPageByNo(pageNo);
        if(page.getIndexes() == null)
            return null;
        page.getIndexes().remove(index);
        return page;
    }

    public LevelDBPage getByIndex(String index){
        Integer pageNo = indexMap.get(index);
        if(null == pageNo){
            return null;
        }
        return getPageByNo(pageNo);
    }

    public LevelDBPage updateIndex(LevelDBPage page, String index){
        // find index map first
        removeIndex(index);
        if(page.getIndexes() == null){
            page.setIndexes(new ArrayList<String>());
        }
        page.getIndexes().add(index);
        indexMap.put(index, (int)page.getNo());
        return page;
    }

    public int size(){
        return this.pages.size();
    }
}
