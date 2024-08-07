package com.aliyun.oss.appservermaven;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;

//import org.junit.Assert;


@SuppressWarnings("deprecation")
@WebServlet(asyncSupported = true)
public class CallbackServer extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5522372203700422672L;

	/**
	 * Get请求 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		url: https://boss-screen-photo.oss-cn-guangzhou.aliyuncs.com/
//		endpoint: oss-cn-guangzhou.aliyuncs.com
//		accessKeyId: LTAI5t6fmgvZvvifAk7oxvgy
//		accessKeySecret: 5RNWkF8uUMfG60q9Me5PdeJ04RCGje
//		bucketName: boss-screen-photo
		String accessId = "LTAI5t6fmgvZvvifAk7oxvgy"; // 请填写您的AccessKeyId。
		String accessKey = "5RNWkF8uUMfG60q9Me5PdeJ04RCGje"; // 请填写您的AccessKeySecret。
		String endpoint = "oss-cn-guangzhou.aliyuncs.com"; // 请填写您的 endpoint。
		String bucket = "boss-screen-photo"; // 请填写您的 bucketname 。
		String host = "http://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
		// callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
		String callbackUrl = "http://120.79.52.53:8992/photo/saveUploadInfo";
		String dir = "photos/"; // 用户上传文件时指定的前缀。

		OSSClient client = new OSSClient(endpoint, accessId, accessKey);
		try {
			long expireTime = 30;
			long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
			Date expiration = new Date(expireEndTime);
			PolicyConditions policyConds = new PolicyConditions();
			policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
			policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

			String postPolicy = client.generatePostPolicy(expiration, policyConds);
			byte[] binaryData = postPolicy.getBytes("utf-8");
			String encodedPolicy = BinaryUtil.toBase64String(binaryData);
			String postSignature = client.calculatePostSignature(postPolicy);

			Map<String, String> respMap = new LinkedHashMap<String, String>();
			respMap.put("accessid", accessId);
			respMap.put("policy", encodedPolicy);
			respMap.put("signature", postSignature);
			respMap.put("dir", dir);
			respMap.put("host", host);
			respMap.put("expire", String.valueOf(expireEndTime / 1000));
			// respMap.put("expire", formatISO8601Date(expiration));

			JSONObject jasonCallback = new JSONObject();
			jasonCallback.put("callbackUrl", callbackUrl);
			jasonCallback.put("callbackBody",
					"filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
			jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
			String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
			respMap.put("callback", base64CallbackBody);

			JSONObject ja1 = JSONObject.parseObject(JSONObject.toJSONString(respMap));
			// System.out.println(ja1.toString());
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST");
			response(request, response, ja1.toString());

		} catch (Exception e) {
			// Assert.fail(e.getMessage());
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 获取public key
	 * 
	 * @param url
	 * @return
	 */
	@SuppressWarnings({ "finally" })
	public String executeGet(String url) {
		BufferedReader in = null;

		String content = null;
		try {
			// 定义HttpClient
			@SuppressWarnings("resource")
			DefaultHttpClient client = new DefaultHttpClient();
			// 实例化HTTP方法
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			content = sb.toString();
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();// 最后要关闭BufferedReader
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return content;
		}
	}

	/**
	 * 获取Post消息体
	 * 
	 * @param is
	 * @param contentLen
	 * @return
	 */
	public String GetPostBody(InputStream is, int contentLen) {
		if (contentLen > 0) {
			int readLen = 0;
			int readLengthThisTime = 0;
			byte[] message = new byte[contentLen];
			try {
				while (readLen != contentLen) {
					readLengthThisTime = is.read(message, readLen, contentLen - readLen);
					if (readLengthThisTime == -1) {// Should not happen.
						break;
					}
					readLen += readLengthThisTime;
				}
				return new String(message);
			} catch (IOException e) {
			}
		}
		return "";
	}

	/**
	 * 验证上传回调的Request
	 * 
	 * @param request
	 * @param ossCallbackBody
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	protected boolean VerifyOSSCallbackRequest(HttpServletRequest request, String ossCallbackBody)
			throws NumberFormatException, IOException {
		boolean ret = false;
		String autorizationInput = new String(request.getHeader("Authorization"));
		String pubKeyInput = request.getHeader("x-oss-pub-key-url");
		byte[] authorization = BinaryUtil.fromBase64String(autorizationInput);
		byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
		String pubKeyAddr = new String(pubKey);
		if (!pubKeyAddr.startsWith("http://gosspublic.alicdn.com/")
				&& !pubKeyAddr.startsWith("https://gosspublic.alicdn.com/")) {
			System.out.println("pub key addr must be oss addrss");
			return false;
		}
		String retString = executeGet(pubKeyAddr);
		retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
		retString = retString.replace("-----END PUBLIC KEY-----", "");
		String queryString = request.getQueryString();
		String uri = request.getRequestURI();
		String decodeUri = java.net.URLDecoder.decode(uri, "UTF-8");
		String authStr = decodeUri;
		if (queryString != null && !queryString.equals("")) {
			authStr += "?" + queryString;
		}
		authStr += "\n" + ossCallbackBody;
		ret = doCheck(authStr, authorization, retString);
		return ret;
	}

	/**
	 * Post请求
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String ossCallbackBody = GetPostBody(request.getInputStream(),
				Integer.parseInt(request.getHeader("content-length")));
		boolean ret = VerifyOSSCallbackRequest(request, ossCallbackBody);
		System.out.println("verify result : " + ret);
		// System.out.println("OSS Callback Body:" + ossCallbackBody);
		if (ret) {
			response(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK);
		} else {
			response(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	/**
	 * 验证RSA
	 * 
	 * @param content
	 * @param sign
	 * @param publicKey
	 * @return
	 */
	public static boolean doCheck(String content, byte[] sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA");
			signature.initVerify(pubKey);
			signature.update(content.getBytes());
			boolean bverify = signature.verify(sign);
			return bverify;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 服务器响应结果
	 * 
	 * @param request
	 * @param response
	 * @param results
	 * @param status
	 * @throws IOException
	 */
	private void response(HttpServletRequest request, HttpServletResponse response, String results, int status)
			throws IOException {
		String callbackFunName = request.getParameter("callback");
		response.addHeader("Content-Length", String.valueOf(results.length()));
		if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
			response.getWriter().println(results);
		else
			response.getWriter().println(callbackFunName + "( " + results + " )");
		response.setStatus(status);
		response.flushBuffer();
	}

	/**
	 * 服务器响应结果
	 */
	private void response(HttpServletRequest request, HttpServletResponse response, String results) throws IOException {
		String callbackFunName = request.getParameter("callback");
		if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
			response.getWriter().println(results);
		else
			response.getWriter().println(callbackFunName + "( " + results + " )");
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}
}
