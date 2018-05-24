package easywin.constant;

public class OutApis {
	public static String sms_verification_verify;
	public static String sms_send;

	// public static void main(String[] args) throws Exception {
	// UrlBuilder urlBuilder = new
	// UrlBuilder(OutApis.oss_file_uploadstream);
	// urlBuilder = urlBuilder.addQueryParam("path",
	// "passion/oss").addQueryParam("srcurl", null)
	// .addQueryParam("ext", "html");
	// Request okHttpRequest = new Request.Builder().url(urlBuilder.getUrl())
	// .post(RequestBody.create(MediaType.parse("text/plain"), "qwe")).build();
	//
	// SysConstant.okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
	// @Override
	// public void onFailure(Call call, IOException e) {
	// logger.debug(ExceptionUtils.getStackTrace(e));
	// }
	//
	// @Override
	// public void onResponse(Call call, Response response) throws IOException {
	// logger.debug("call out api：" + response.request().url() + " <--- " +
	// response.body().string());
	//
	// }
	//
	// });
	// }
	//
	// public static class OssUploadStreamRes {
	// public Integer code = null;
	// public String error = null;
	// public String requestId = null;
	// public Data data = null;
	//
	// public static class Data {
	// public String url = null;
	// }
	//
	// }
	//
	//// public static OssUploadStreamRes ossUploadStream(String path, String
	// srcurl, String ext, InputStream ins)
	//// throws Exception {
	////
	//// Map<String, String> queryParams = new HashMap<String, String>();
	//// if (path != null)
	//// queryParams.put("path", path);
	//// if (srcurl != null)
	//// queryParams.put("srcurl", srcurl);
	//// if (ext != null)
	//// queryParams.put("ext", ext);
	//// Map<String, String> headers = new HashMap<String, String>();
	//// headers.put("content-type", "text/plain");
	//// String result = HttpUtils.post(oss_file_uploadstream, headers,
	// queryParams, ins);
	//// logger.debug("外部接口ossUploadStream返回结果：" + result);
	//// if (StringUtils.isEmpty(result))
	//// return null;
	//// return JSON.parseObject(result, OssUploadStreamRes.class);
	//// }
	//
	// public static OssUploadStreamRes smsSmsSend(String phone, String content,
	// InputStream ins) throws Exception {
	// Map<String, String> queryParams = new HashMap<String, String>();
	// queryParams.put("phone", phone);
	// queryParams.put("content", content);
	// Map<String, String> headers = new HashMap<String, String>();
	// // headers.put("content-type", "text/plain");
	// String result = HttpUtils.post(sms_sms_send, headers, queryParams, ins);
	// logger.debug("外部接口ossUploadStream返回结果：" + result);
	// if (StringUtils.isEmpty(result))
	// return null;
	// return JSON.parseObject(result, OssUploadStreamRes.class);
	// }
}
