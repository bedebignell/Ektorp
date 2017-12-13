package org.ektorp.http.clientconfig;

import org.apache.http.client.HttpClient;

public class OldHttpClientRequestExecutor extends HttpClientRequestExecutor {

	private org.apache.http.client.HttpClient client;

	private org.apache.http.client.HttpClient backend;

	public OldHttpClientRequestExecutor() {
		super();
	}

	public OldHttpClientRequestExecutor(org.apache.http.client.HttpClient client, org.apache.http.client.HttpClient backend) {
		super();
		this.client = client;
		this.backend = backend;
	}

	@Override
	public HttpClient locateHttpClient(boolean useBackend) {
		if (useBackend) {
			return backend;
		} else {
			return client;
		}
	}

	@Override
	public void releaseHttpClient(HttpClient client) {
		// nothing to do
	}

	@Override
	public void shutdown() {
		client.getConnectionManager().shutdown();
		backend.getConnectionManager().shutdown();
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setBackend(HttpClient backend) {
		this.backend = backend;
	}

}
