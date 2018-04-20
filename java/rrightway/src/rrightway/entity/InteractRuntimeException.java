package rrightway.entity;

public class InteractRuntimeException extends RuntimeException {
	private int code;
	private Object data;

	public InteractRuntimeException(int code) {
		super();
		this.code = code;
	}

	public InteractRuntimeException(String message, Object data) {
		super();
		this.code = 99;
		this.data = data;
	}

	public InteractRuntimeException(int code, Object data) {
		super();
		this.code = code;
		this.data = data;
	}

	public InteractRuntimeException(String message, Throwable cause) {
		super(message, cause);
		this.code = 99;
		// TODO Auto-generated constructor stub
	}

	public InteractRuntimeException(String message) {
		super(message);
		this.code = 99;
		// TODO Auto-generated constructor stub
	}

	public InteractRuntimeException(Throwable cause) {
		super(cause);
		this.code = 99;
		// TODO Auto-generated constructor stub
	}

	public int getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}

}
