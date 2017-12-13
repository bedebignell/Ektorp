package org.ektorp.http.clientconfig;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdHttpResponse;

import java.io.IOException;

public abstract class HttpClientRequestExecutor {

	public HttpResponse executeRequest(HttpUriRequest request, boolean useBackend) throws IOException {
		HttpClient client = null;
		try {
			client = locateHttpClient(useBackend);

			final org.apache.http.HttpResponse response;
			if (useBackend) {
				response = client.execute(request);
			} else {
				response = client.execute(getHttpHost(client), request);
			}

			return createHttpResponse(response, request);
		} finally {
			releaseHttpClient(client);
		}
	}

	public HttpHost getHttpHost(HttpClient client) {
		return (HttpHost) client.getParams().getParameter(ClientPNames.DEFAULT_HOST);
	}

	public abstract HttpClient locateHttpClient(boolean useBackend);

	public abstract void releaseHttpClient(HttpClient client) throws IOException;

	public HttpResponse createHttpResponse(org.apache.http.HttpResponse rsp, HttpUriRequest httpRequest) {
		return new StdHttpResponse(rsp.getEntity(), rsp.getStatusLine(), httpRequest, rsp.getFirstHeader("ETag"));
	}

	public abstract void shutdown();

}
