package org.ektorp.http.clientconfig;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;

public class ConfigurableHttpRequestRetryHandler implements HttpRequestRetryHandler {

	protected final static Logger LOG = LoggerFactory.getLogger(ConfigurableHttpRequestRetryHandler.class);

	// usage example : 
	//{
	//	DefaultHttpClient httpClient = new DefaultHttpClient();
	//	httpClient.setHttpRequestRetryHandler(new CustomHttpRequestRetryHandler());
	//}

	/**
	 * a default value of 2 retries should be good for most cases
	 */
	private static final int DEFAULT_MAX_RETRY_COUNT = 2;

	private int maxRetryCount;

	private boolean includeExceptionInLogs = false;

	public ConfigurableHttpRequestRetryHandler() {
		this(DEFAULT_MAX_RETRY_COUNT);
	}

	public ConfigurableHttpRequestRetryHandler(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		final HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
		boolean result = computeRetryRequest(exception, executionCount, context, request);

		final String method = request.getRequestLine().getMethod();
		if (includeExceptionInLogs) {
			LOG.info("Decide whether to retry {} request attempt #{} = {} for exception {} : {}", new Object[]{method, executionCount, result, exception.getClass().getName(), exception.getMessage(), exception});
		} else {
			LOG.info("Decide whether to retry {} request attempt #{} = {} for exception {} : {}", new Object[]{method, executionCount, result, exception.getClass().getName(), exception.getMessage()});
		}

		return result;
	}

	private boolean computeRetryRequest(IOException exception, int executionCount, HttpContext context, HttpRequest request) {
		if (executionCount >= maxRetryCount) {
			// Do not retry if over max retry count
			return false;
		} else if (exception instanceof NoHttpResponseException) {
			// Retry if the server dropped connection on us
			return true;
		} else if (exception instanceof SSLHandshakeException) {
			// Do not retry on SSL handshake exception
			return false;
		} else if (exception instanceof ConnectionClosedException) {
			// Caused by: org.apache.http.ConnectionClosedException: Premature end of Content-Length delimited message body (expected: 88; received: 0 
			return true;
		} else if (exception instanceof SSLPeerUnverifiedException) {
			// javax.net.ssl.SSLPeerUnverifiedException: Host name does not match the certificate subject provided by the peer
			// this is a permanent Exception, a retry will not have more chance to succeed, so we do not retry
			return false;
		}

		boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
		// Retry if the request is considered idempotent
		return idempotent;
	}

	public void setIncludeExceptionInLogs(boolean includeExceptionInLogs) {
		this.includeExceptionInLogs = includeExceptionInLogs;
	}
}
