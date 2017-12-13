package org.ektorp.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author henriklundgren
 */
public class StdHttpResponse implements HttpResponse {

	private final static Logger LOG = LoggerFactory.getLogger(StdHttpResponse.class);

	private final static HttpEntity NULL_ENTITY = new NullEntity();

	private final HttpEntity entity;
	private final StatusLine status;
	private final String requestURI;
	private final HttpUriRequest httpRequest;
	private final String revision;

	private boolean released = false;

	private ConnectionReleasingInputStream inputStream;

	private final Throwable instantiationStackTrace = new Throwable("This is the instantiation stack trace of the StdHttpResponse instance");

	public static StdHttpResponse of(org.apache.http.HttpResponse rsp, HttpUriRequest httpRequest) {
		return new StdHttpResponse(rsp.getEntity(), rsp.getStatusLine(), httpRequest, rsp.getFirstHeader("ETag"));
	}

	public StdHttpResponse(HttpEntity e, StatusLine status, HttpUriRequest httpRequest, Header eTagHeader) {
		this.httpRequest = httpRequest;
		this.entity = e != null ? e : NULL_ENTITY;
		this.status = status;
		this.requestURI = httpRequest.getURI().toString();
		if (eTagHeader != null) {
			revision = eTagHeader.getValue().replace("\"", "");
		} else {
			revision = null;
		}
	}


	public int getCode() {
		return status.getStatusCode();
	}

	public String getReason() {
		return status.getReasonPhrase();
	}

	public String getRequestURI() {
		return requestURI;
	}

	public long getContentLength() {
		return entity.getContentLength();
	}

	public String getContentType() {
		return entity.getContentType().getValue();
	}

	/**
	 * TODO : add IOException to method signature
	 */
	public InputStream getContent() {
		if (inputStream == null) {
			try {
				inputStream = new ConnectionReleasingInputStream(entity.getContent(), this);
			} catch (IOException e) {
				throw Exceptions.propagate(e);
			}
		}
		return inputStream;
	}

	public String getETag() {
		return revision;
	}

	public boolean isSuccessful() {
		return getCode() < 300;
	}

	public void releaseConnection() {
		ConnectionReleasingInputStream content = null;
		try {
			content = (ConnectionReleasingInputStream) getContent();
		} finally {
			if (content != null) {
				if (!content.isClosed()) {
					IOUtils.closeQuietly(content);
				}
			}
			released = true;
		}
	}

	public void abort() {
		httpRequest.abort();
	}

	public String toString() {
		return status.getStatusCode() + ":" + status.getReasonPhrase();
	}

	@Override
	protected void finalize() {
		if (!released) {
			LOG.warn("StdHttpResponse was not released properly. In order to avoid leaking connections, don't forget to call releaseConnection() on every instance of StdHttpResponse", instantiationStackTrace);
		}
	}

	private static class NullEntity implements HttpEntity {

		private static final Header contentType = new BasicHeader(HTTP.CONTENT_TYPE, "null");

		private static final Header contentEncoding = new BasicHeader(HTTP.CONTENT_ENCODING, "UTF-8");

		public void consumeContent() throws IOException {

		}

		public InputStream getContent() throws IOException,
				IllegalStateException {
			return null;
		}

		public Header getContentEncoding() {
			return contentEncoding;
		}

		public long getContentLength() {
			return 0;
		}

		public Header getContentType() {
			return contentType;
		}

		public boolean isChunked() {
			return false;
		}

		public boolean isRepeatable() {
			return true;
		}

		public boolean isStreaming() {
			return false;
		}

		public void writeTo(OutputStream outstream) throws IOException {
			throw new UnsupportedOperationException("NullEntity cannot write");
		}

	}

}
