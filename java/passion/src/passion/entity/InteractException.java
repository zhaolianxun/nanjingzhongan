package passion.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;

public class InteractException extends RuntimeException {
	private int code;
	private Object data;

	public InteractException(int code) {
		super();
		this.code = code;
	}

	public InteractException(int code, Object data) {
		super();
		this.code = code;
		this.data = data;
	}

	public InteractException(String message, Throwable cause) {
		super(message, cause);
		this.code = 99;
		// TODO Auto-generated constructor stub
	}

	public InteractException(String message) {
		super(message);
		this.code = 99;
		// TODO Auto-generated constructor stub
	}

	public InteractException(Throwable cause) {
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
