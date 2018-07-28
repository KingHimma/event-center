package eventcenter.leveldb;

import java.io.Serializable;

public class LevelDBCursor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -348756876229797827L;

	private long pageNo;
	
	private long index;

	public long getPageNo() {
		return pageNo;
	}

	public void setPageNo(long pageNo) {
		this.pageNo = pageNo;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}
}
