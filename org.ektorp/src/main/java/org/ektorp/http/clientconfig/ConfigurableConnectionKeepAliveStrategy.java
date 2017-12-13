package org.ektorp.http.clientconfig;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class is inspired by code found at : http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e412
 */
public class ConfigurableConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

	private final static Logger LOG = LoggerFactory.getLogger(ConfigurableConnectionKeepAliveStrategy.class);

	/**
	 * a default value of 60 seconds should be good for most cases.<br>
	 * Users should use the setDurationInSecondForHost(long) in order to set a custom value.<br>
	 */
	private static final long DEFAULT_DEFAULT_DURATION_IN_SECONDS = 60;

	private final Map<String, Long> durationPerHostNameMap = new ConcurrentHashMap<String, Long>();

	private long defaultDurationInSeconds;

	public ConfigurableConnectionKeepAliveStrategy() {
		this(DEFAULT_DEFAULT_DURATION_IN_SECONDS);
	}

	public ConfigurableConnectionKeepAliveStrategy(long defaultDurationInSeconds) {
		super();
		this.defaultDurationInSeconds = defaultDurationInSeconds;
	}

	@Override
	public long getKeepAliveDuration(HttpResponse response, org.apache.http.protocol.HttpContext context) {
		long result = computeKeepAliveDurationInSeconds(response, context);
		return result * 1000;
	}

	public Long getDurationInSecondForHost(String hostname) {
		return this.durationPerHostNameMap.get(hostname);
	}

	public void setDurationInSecondForHost(String hostname, Long duration) {
		this.durationPerHostNameMap.put(hostname, duration);
	}

	/**
	 * The default duration (in seconds) to keep connections alive. This is the default value, that is used when the server hostname was not registered with a custom duration.
	 */
	public long getDefaultDurationInSeconds() {
		return defaultDurationInSeconds;
	}

	public void setDefaultDurationInSeconds(long defaultDurationInSeconds) {
		this.defaultDurationInSeconds = defaultDurationInSeconds;
	}

	private long computeKeepAliveDurationInSeconds(HttpResponse response, org.apache.http.protocol.HttpContext context) {
		// Honor 'keep-alive' header if present HttpResponse's header
		for (HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE)); it.hasNext(); ) {
			HeaderElement he = it.nextElement();
			String param = he.getName();
			String value = he.getValue();
			if (value != null && param.equalsIgnoreCase("timeout")) {
				try {
					return Long.parseLong(value) * 1000;
				} catch (NumberFormatException e) {
					LOG.warn("NumberFormatException while parsing the " + HTTP.CONN_KEEP_ALIVE + " header : " + value, e);
				}
			}
		}

		final HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
		final String hostName = target.getHostName();

		final Long durationInSecondsForHost = getDurationInSecondForHost(hostName);
		if (durationInSecondsForHost == null) {
			return defaultDurationInSeconds;
		} else {
			return durationInSecondsForHost;
		}
	}

}
