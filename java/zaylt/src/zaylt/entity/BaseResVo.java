package zaylt.entity;

/**
 * 
 * @author rxw
 *
 */
public class BaseResVo {

	private String codeMsg = null;
	private Integer code = null;
	private String requestId = "";
	private Object data = new Object();

	public String getCodeMsg() {
		return codeMsg;
	}

	public Integer getCode() {
		return code;
	}

	public String getRequestId() {
		return requestId;
	}

	public Object getData() {
		return data;
	}

	public void setCodeMsg(String codeMsg) {
		this.codeMsg = codeMsg;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
