package passion.entity;

/**
 * 增量分页
 * @author xinxin
 *
 */
public class IncrPage {
	private Integer pageSize;
	private Integer toPageNo;
	private Integer start;
	private String lastRowId;
	private String firstRowId;
	private String septum;
	private String orderType;
	private Long totalItemCount;
	private Long totalPageCount;

	public IncrPage(Integer pageSize, Integer toPageNo, String lastRowId, String firstRowId, String septum,
			String orderType) {
		this.pageSize = pageSize;
		this.toPageNo = toPageNo;
		this.lastRowId = lastRowId;
		this.firstRowId = firstRowId;
		this.septum = septum;
		this.orderType = orderType;
		if (toPageNo == null)
			start = 0;
		else {
			start = pageSize * (toPageNo - 1);
		}
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public Integer getToPageNo() {
		return toPageNo;
	}

	public Integer getStart() {
		return start;
	}

	public String getLastRowId() {
		return lastRowId;
	}

	public String getFirstRowId() {
		return firstRowId;
	}

	public String getSeptum() {
		return septum;
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

	public void setTotalItemCount(Long totalItemCount) {
		this.totalItemCount = totalItemCount;
		this.totalPageCount = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
	}

}
