package demo;

import java.io.StringReader;
import java.security.Security;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.qq.weixin.mp.aes.WXBizMsgCrypt;

public class Program {

	public static void main(String[] args) throws Exception {

		//
		// 第三方回复公众平台
		//
		Security.addProvider(new BouncyCastleProvider());
		// 需要加密的明文
		String encodingAesKey = "jjkkkkjkjkjkjk3434348989891212121212893232d";
		String token = "ldkfjxmcnvjgutiroe03948571iqkeu4";
		String timestamp = "1517455365";
		String nonce = "1864519582";
		String appId = "wx14ce9af359895e9f";
		String replyMsg = " 中文<xml><ToUserName><![CDATA[oia2TjjewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType><![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Description><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>";

		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
//		String mingwen = pc.encryptMsg(replyMsg, timestamp, nonce);
//		System.out.println("加密后: " + mingwen);
//
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		DocumentBuilder db = dbf.newDocumentBuilder();
//		StringReader sr = new StringReader(mingwen);
//		InputSource is = new InputSource(sr);
//		Document document = db.parse(is);
//
//		Element root = document.getDocumentElement();
//		NodeList nodelist1 = root.getElementsByTagName("Encrypt");
//		NodeList nodelist2 = root.getElementsByTagName("MsgSignature");
//
//		String encrypt = nodelist1.item(0).getTextContent();
//		String msgSignature = nodelist2.item(0).getTextContent();
//
//		String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
//		String fromXML = String.format(format, encrypt);
//System.out.println(fromXML);
		//
		// 公众平台发送消息给第三方，第三方处理
		//
String fromXML="<xml><AppId><![CDATA[wx14ce9af359895e9f]]></AppId><Encrypt><![CDATA[Kow651s6DNykOPJlB+ppOR/X404REHn7bBl7rGcxO/xxHENeDpFZVrFjpz8mykyv7LO9xBbgyoGHoFlB0ghEdvEMimR8p5h4AJ9zEBzLVi2kmviOSgT3USEGLx3D8oi3ZGMAUJIOT3Pwkkdbw0VwoR6/hN0Y6phcCgBWZwQtrlGc341tf3gQyvd4fOl7XUv+lWwGWuTd+lp4RBG1TqqqgamLij+s2lGGdQUPyLvDDQOtF5SqDQ+X1OqGiP486Ph1+bPIYD0BQdUcLWNHqsKD6iUlwznq0ik3NR7rLyeD018l++PTlIm+BT/Vj5DNwL1YSOCD7VTB4/HSFdAeYtsjNN2CBJA1KtxytAm2aQBhKS7yufbHfktikrmKzSskePNmJXvEgmAYd+f7lJGWvhWYAWxUt3txHGnb0ygCyK1oCSP9AIs3SwsnY8iG7WV6pSiQoXj8Ucyyzb77WK2lLAWm4Q==]]></Encrypt></xml>";
		// 第三方收到公众号平台发送的消息
		String result2 = pc.decryptMsg("fbc4310139e549b7fce7f4c80d8267f9c989a0fb", timestamp, nonce, fromXML);
		System.out.println("解密后明文: " + result2);
		
		//pc.verifyUrl(null, null, null, null);
	}
}
