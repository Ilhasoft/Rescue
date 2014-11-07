package br.com.ilhasoft.rescue.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class WebService {
	
	private DefaultHttpClient httpClient;
	private HttpContext localContext;
	private String ret;

	private HttpParams myParams = null;
	private HttpResponse response = null;
	private HttpPost httpPost = null;
	private HttpGet httpGet = null;
	private String webServiceUrl;
	
	public WebService(String serviceName, int timeout){
		myParams = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(myParams, timeout);
		HttpConnectionParams.setSoTimeout(myParams, timeout);
		httpClient = new DefaultHttpClient(myParams);
		localContext = new BasicHttpContext();
		webServiceUrl = serviceName;
	}

	public String webInvoke(String methodName, String data, String contentType) throws IOException, Exception {
		ret = null;

		httpPost = new HttpPost(webServiceUrl + methodName);
		httpPost.setParams(myParams);
		response = null;

		StringEntity tmp = null;

		if (contentType != null) {
			httpPost.setHeader("Content-Type", contentType);
		} else {
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		}

		try {
			tmp = new StringEntity(data);
		} catch (UnsupportedEncodingException e) {
			Log.e("Groshie", "HttpUtils : UnsupportedEncodingException : "+e);
		}

		httpPost.setEntity(tmp);

		Log.d("Groshie", webServiceUrl + "?" + data);

		response = httpClient.execute(httpPost, localContext);

		if (response != null) {
			ret = EntityUtils.toString(response.getEntity());
		}

		return ret;
	}

	public String webGet() throws IOException, Exception {
		String getUrl = webServiceUrl;

		httpGet = new HttpGet(getUrl);
		Log.e("WebGetURL: ",getUrl);

		response = httpClient.execute(httpGet);
		ret = EntityUtils.toString(response.getEntity());

		return ret;
	}
	
	public void abort() throws Exception {
		if(httpPost != null) {
			httpPost.abort();
		}
	}
}