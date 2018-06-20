package cszyylxm.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebEngine {

	Document mydoc = null;

	public WebEngine(String url) throws MalformedURLException, IOException {
		mydoc = Jsoup.parse(new URL(url), 30000);// 利用Jsoup实现document树
	}

	public Element getElementById(String id) {// 实现document的getElementById方法
		System.out.println("Java println:\t" + mydoc.getElementById(id));
		return mydoc.getElementById(id);// 返回的是Element对象
	}

	public static void main(String[] args) throws MalformedURLException, IOException {

		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("javascript"); // 初始化Java内置的javascript引擎
		try {

			se.eval("function test() {"
					+ "id='areaDefault'; print('js print:'+document.getElementById(id));return document.getElementById(id);}");
			// 测试用javascript自定义函数，功能为输出id为areaDefault的元素，并返回。
			Invocable invocableEngine = (Invocable) se;// 转换引擎类型为Invocable
			se.put("document", new WebEngine("http://www.ifeng.com/")); // 关联对象，这一步很重要，关联javascript的document对象为TaoDocument，亦即我自己实现的document对象
			Element callbackvalue = (Element) invocableEngine.invokeFunction("test"); // 直接运行函数，返回值为Element
			System.out.println("callback return :" + callbackvalue); // 打印输出返回内容
			se.eval("test()");// 另外一种调用函数方式，我更偏爱此种方式
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}