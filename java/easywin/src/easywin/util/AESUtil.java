package easywin.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author jing.huang
 * @function
 * @date 2018年1月10日
 * @version
 */
public class AESUtil {
	/**
	 * 密钥算法
	 */
	private static final String ALGORITHM = "AES";
	/**
	 * 加解密算法/工作模式/填充方式
	 */
	private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS5Padding";

	// key 2IBtBXdrqC3kCBs4gaceL7nl2nnFadQv
	/**
	 * AES加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String encryptData(String data, String key) throws Exception {
		// 创建密码器
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
		// 初始化
		SecretKeySpec keyMD5 = new SecretKeySpec(MD5.MD5Encode(key).toLowerCase().getBytes(), ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, keyMD5);
		return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
	}

	/**
	 * AES解密
	 * 
	 * @param base64Data
	 * @return
	 * @throws Exception
	 */
	public static String decryptData(String base64Data, String key) throws Exception {
		SecretKeySpec keyMD5 = new SecretKeySpec(MD5.MD5Encode(key).toLowerCase().getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, keyMD5);
		return new String(cipher.doFinal(Base64.getDecoder().decode(base64Data)));
	}

	public static void main(String[] args) throws Exception {
		// 解密
		// String
		// req_info="Ih5osM/5IbPfHouVrUmwebd1yAW2Gys91jv006W1237sSi3z022KxHafLIDMrQLYiBttTadgvy2cbx6DnmwDIQ52lPWfo6pAAHt7Q9DjBIpDRQ7JsbEBlomoQP2ZkdNHnWscVYuFEVlItaSlkSlcKLdB4UwMduqDYseFsUUthz6htPeBu987zXS6dKrgIbRwOxt5RfPmk1sf0oVB2yU3UH0Ly8SzBjmN1jrh4qAaUkfH6VkeMJcsZSGchQn2VresxJTbGH++JE1UsXUF3gyYpweyxBPtHoKdaggsIONR20UKNxJYPJLnEOnfQF/Ipmk8/QmTVRK7iqfVLC9EA1Auma0AlKBjZlYqynUlF3y+E2ZzgWMUlvDHZVWDbzp/TcE0q+Ukc7yQ3HBsibDR474SPlLTkCWz1iydXzkVcLqJKamsh76Liv1a0hzu0sI3qasMAfmwU6/q7/N6quq031toO1GxqkVaxBRK7e64gSOx9ArxxVFgZ7WN+JPq2OH/pTKH8ToxHA0rtxN5+aAgZGkXiIOUiHtp4mjpRxqe34WK7C7Nr0DQyOVwsXT2TTegSgWGm34aa//ZYxHedubv2iX+E7K222lptg9IqHlMXBbwKFtKtIcal61+8ciz+sB1FBpqHchC+3whTqWv5ZANiHBzaOhbIbA/mKX2XZ6Cy0iYh+bL/8Y/Hvz/UnMGzor+2anIUeBAGRQmseL4jY+Qic46WLuEhDcarCaO4JgJSAOC+VmsdrER9TRum26PFwTQwtNpxkrKCiO9Gv36Ood5D8hXnLHUH+4nbsek8ouxkCcFXq4Us0mipB3i5ksQpt23LiJm9Ahxyvptp9Q41SytS48NXiz3IxTOqDdknowedZwAtJ/fhBlwiOHD9N+pECXuNBKLaCZcatGycr0/DPELiCF+MIRQ6V60wzaZD74TKRFULd1ljNsoQIAbuGaT40WMDY6a28jBHQ/IXnD4gvSvfeumwQzp3Q9PiPyFtF6JxH7RBRj9/lmQuQozJIPZCaCNVTBfWQOdcFaBnPLN0ZNvzjA93g6jcIxHzkXHmiGfh98vq2E=";
		String req_info = "m4NnwrtY0jhpDgNp65H1V/0OWMtSoTYhhY89MHbflhmnaHq9ZKjx9ABq6Jpg4SccA876HVy7J9P85NpdvCMNGInZ4fANDRE+YfZe4HeF+bbFj6JETcEFPpE9YW+bTbC0D+gl/otScJfvB2QUK7+EeBGPHN1EWX9zbr2Gw6AUaORdFk3mGxV5dtjuwWQrv5juzkXDs33Z2dUMslO+i3j0cTDHqwS4hptx2j6h2HvzgzltFbjo7nysU+4rArqJvrGW/9r18e1St9XgG21NALqixfaSmqetOR4zLVL4/+z3CEz8cg5r+/4GUOTf3XFmLCZ/wEkRQhKRNVibG0NFfiG3KnqArMJ/dheQHCd7qL+XX/ZV6tj8RLMgL7R6hOiR03Ljyikdxq9M3K9CTYgf03pHJd3geXX1LgXrLxc1flL6NW+zD3ZayGYpr1WpLsSMG7z8W5j1pme6cRj3n2+CwSFnOnOkxaFuLKoJAJIqM3gbC0eN++vY73RKphlI4zZqg6o5s3MXI6ju1yoi/ZQ+XbTg2JttsdbU0aySernKwkt0rYMf0j/Mcvo2axgHbI3w/iTm141WxHUjkQ+ga2X1pOWdGifGhSmMP8oGaA/WD5MAsK1qXX0eFvUWS/PTauCSTWq5Cmr8loA/KL3jgvB0nyR4mfccB+tPy4Ny7kzOlr/VNeb0ULf96R0AWFWCtdt8AmujAP0DYiM5FSmYLI0XRhpSDjnEbBM8+isNE1GlAVR3NzzemwQORihScovpAktbRSN/d3N+NgTjSoVDiJvCOxCs3thX9qt9iwYbA+/X/gv8lza2FZyIzwkQxGRcYl8JWKpXzNW8EWUNVnSLdHvQttDeV3CvgP/x579RGd6whyFYS6AaI0qw7oTjCFh2EHS/VzGvFuv166ZlVIJ4MNvg79O9h63ZOSE1LzVqEsVh8fDCfM2GgJ9aUdl95Djgunit4yIZOdoigR3f/BEHKrYCEham11rYohaAXs4XAXWihsV3WD5j4G/P+txvjAwujvf4HDwzHgFsmSml013U2mUiy+v4zw==";
		String a = "/79VKDt22gfnjpM6GXtdw7wLyK8gfFLqwYGJcMBNs/8RcqSqmRec5QDetIxBwdCoUpP9wi8D6JND3Qj46V2UA7ykvFrsg3j/i6aG4NS/uJoZ5r0XVNjVPFIYc4TPgytwEXKkqpkXnOUA3rSMQcHQqLsWa1V+4HLTXUyBnAElrDx69r9Q71CWViFzkzZJqi42URv1sBzQuUZ3g4aCSjVcz+w8hf20jTVlicHdm6qrQYLQoOXRnz8v8pOply4fz4mSEsSqM0i4O9PL6mpMhrxnsY3fnJSNDYKe2kI9BB7kPDP9ORAxkGSQsodSapX2G2r0tX/Okv3+BUszWevH3pz9QR7hVnTEv/4B0VGrhfH2UHdPsEz62dkPNGXdai9J5Sjg8Xl+KeQGAR/G3IzkeeXOZrl/Q4MQJtNogQ/XAFYVyKVaOKUN06JPL1ubbTLZB7NTuiW+tcr3Dtup6L4v1Y+huK8feCfFM6PPclB7pqx/wiZy91dXK8/mpzB/3JW8k1tq7B9hQKDoTFYHRHhv3aj7pGx5rlhUwjs4eu1dwcatpEsxIxOiGLQCrpJOi7C9aHEszkklE+wTtIPfxhSxOCeYEHF/SPhS0yJg2QmpylfrGOhrafET4yjpWcEJ6YibFOzOYbFrHr6V1yRsZKee+F9BVp3qHPU9+obsfh06E1BghJ6GoAyQ9HfpP/NDIlMAhBXtAm7wlXzj6C73FJ+Aacko60VMEfyiR15tBm5xMdWyB7uS2hL5qP0wX0k2dunHhiNrVNwEFlqrMA4LAIA4vkCULvxYNBNQVqC1K7P22LQjvpTDdMEjPS988mrYw/H91ys78v+E+YPhKgjAEfzEJEpBXbSs1USv3PAkCVIUt6s9fdadrhMnJIFuedBo37zqyaHz6vC1irtSg9Qzy04ibCMv+Q31685OhBNRjo8m5/DzmZ726VMZfBJppWIusNPY2p2qcyTZoKuBuYXnnkq440YKmv6GuwNg3ZTJlBT6+dpFDTiD4/401IXYEcpTmw0UvhlwtPtHIgyXu1Cv2f6rqv3Mb1+nshoCNqyNy+0Fsci3pwk=";

		String B = AESUtil.decryptData(a, "YfRf123weq1reADKLYRERQAC9ra8vh61");
		System.out.println(B);

		// 加密
		String str = "<root>" + "<out_refund_no><![CDATA[2531340110812300]]></out_refund_no>"
				+ "<out_trade_no><![CDATA[2531340110812100]]></out_trade_no>"
				+ "<refund_account><![CDATA[REFUND_SOURCE_RECHARGE_FUNDS]]></refund_account>"
				+ "<refund_fee><![CDATA[1]]></refund_fee>"
				+ "<refund_id><![CDATA[50000505542018011003064518841]]></refund_id>"
				+ "<refund_recv_accout><![CDATA[支付用户零钱]]></refund_recv_accout>"
				+ "<refund_request_source><![CDATA[API]]></refund_request_source>"
				+ "<refund_status><![CDATA[SUCCESS]]></refund_status>"
				+ "<settlement_refund_fee><![CDATA[1]]></settlement_refund_fee>"
				+ "<settlement_total_fee><![CDATA[1]]></settlement_total_fee>"
				+ "<success_time><![CDATA[2018-01-10 10:31:24]]></success_time>"
				+ "<total_fee><![CDATA[1]]></total_fee>"
				+ "<transaction_id><![CDATA[4200000052201801101409025381]]></transaction_id>" + "</root>";
		System.out.println(encryptData(str, ""));
	}

}
