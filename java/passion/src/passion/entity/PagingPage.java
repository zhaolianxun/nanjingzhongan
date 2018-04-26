package passion.entity;

/**
 * 页码分页
 * 
 * @author xinxin
 *
 */
public class PagingPage {
	private long pageSize;
	private long pageNo;
	private long start;
	private String orderType;
	private long totalItemCount;
	private long totalPageCount;

	public PagingPage(long pageSize, long pageNo, long totalItemCount) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.totalItemCount = totalItemCount;

		if (pageSize < 0)
			this.pageSize = 0;
		if (pageSize > 500)
			this.pageSize = 500;
		if (this.pageSize > 0)
			this.totalPageCount = this.totalItemCount % this.pageSize > 0 ? this.totalItemCount / this.pageSize + 1
					: this.totalItemCount / this.pageSize;
		else
			this.totalPageCount = 0l;

		if (pageNo >= 1)
			this.start = this.pageSize * (this.pageNo - 1);
		else {
			this.start = 0;
			this.pageSize = 0;
		}
	}

	public Long getPageSize() {
		return pageSize;
	}

	public Long getPageNo() {
		return pageNo;
	}

	public Long getStart() {
		return start;
	}

	public String getOrderType() {
		return orderType;
	}

	public Long getTotalItemCount() {
		return totalItemCount;
	}

	public Long getTotalPageCount() {
		return totalPageCount;
	}

}
