package org.ektorp.impl;

import org.apache.commons.io.IOUtils;
import org.ektorp.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

public class ResponseOnFileStub implements HttpResponse {

	public static class CloseMonitoringInputStream extends InputStream {

		private boolean closed = false;

		private boolean eof = false;

		private final InputStream in;

		public CloseMonitoringInputStream(InputStream in) {
			super();
			this.in = in;
		}

		public boolean isClosed() {
			return closed;
		}

		public boolean isEOF() {
			return eof;
		}

		@Override
		public int read() throws IOException {
			int read = in.read();
			eof = (read == -1);
			return read;
		}

		@Override
		public int read(byte b[], int off, int len) throws IOException {
			int read = in.read(b, off, len);
			eof = (read < len);
			return read;
		}

		@Override
		public void close() throws IOException {
			in.close();
			this.closed = true;
		}

	}

	public static ResponseOnFileStub newInstance(int code, String fileName) {
		ResponseOnFileStub r = newInstance(code, fileName, "application/json", -1);
		r.fileName = fileName;
		return r;
	}

	public static ResponseOnFileStub newInstance(int code, String fileName, String contentType, int contentLength) {
		ResponseOnFileStub r = newInstance(code, ResponseOnFileStub.class.getResourceAsStream(fileName), contentType, contentLength);
		r.fileName = fileName;
		return r;
	}

	public static ResponseOnFileStub newInstance(int code, InputStream in, String contentType, int contentLength) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		if (in != null) {
			r.in = new CloseMonitoringInputStream(in);
		}
		r.contentLength = contentLength;
		r.contentType = contentType;
		return r;
	}

	private int code;

	private CloseMonitoringInputStream in;

	private boolean connectionReleased;

	private String contentType = "application/json";

	private int contentLength;

	private String fileName;

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getContent() {
		return in;
	}

	@Override
	public String getETag() {
		return null;
	}

	@Override
	public boolean isSuccessful() {
		return code < 300;
	}

	@Override
	public void abort() {
		releaseConnection();
	}

	@Override
	public void releaseConnection() {
		connectionReleased = true;
		IOUtils.closeQuietly(in);
	}

	public boolean isConnectionReleased() {
		return connectionReleased;
	}

	@Override
	public long getContentLength() {
		return contentLength;
	}

	@Override
	public String getRequestURI() {
		return "static/test/path";
	}

	@Override
	public String toString() {
		return fileName;
	}
}
