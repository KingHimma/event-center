package eventcenter.leveldb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * it store a page which include some indexes and page no
 * @author JackyLIU
 *
 */
public class LevelDBPage implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3661168670876518992L;

	private long no;
	
	private List<String> indexes;

	public long getNo() {
		return no;
	}

	public void setNo(long no) {
		this.no = no;
	}

	public List<String> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<String> indexes) {
		this.indexes = indexes;
	}

	@Override
	public LevelDBPage clone() {
		LevelDBPage page = new LevelDBPage();
		page.setNo(no);
		if(null != this.indexes){
			List<String> dest = new ArrayList<String>(this.indexes.size() + 1);
			page.setIndexes(dest);
			for(String index : this.indexes){
				dest.add(index);
			}
		}
		return page;
	}
}
