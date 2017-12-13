package org.ektorp.http;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConnectionReleasingInputStreamTest {

	@Test
	public void shouldAcceptBothParameterNull() throws IOException {
		ConnectionReleasingInputStream stream = new ConnectionReleasingInputStream(null, null);
		stream.close();
	}


	@Test
	public void shouldAcceptNullUnderlyingStream() throws IOException {
		InputStream underlyingStream = null;
		HttpResponse httpResponse = mock(HttpResponse.class);
		ConnectionReleasingInputStream stream = new ConnectionReleasingInputStream(underlyingStream, httpResponse);
		stream.close();
		verify(httpResponse, times(1)).releaseConnection();
	}

	@Test
	public void shouldAcceptNullHttpResponse() throws IOException {
		InputStream underlyingStream = new NullInputStream(0);
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = new ConnectionReleasingInputStream(underlyingStream, httpResponse);
		stream.close();
	}

	@Test
	public void shouldConsumeAutomaticallyIfEOFWasNotReached() throws IOException {
		InputStream underlyingStream = new ReaderInputStream(new StringReader("hello world"));
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		// avoids logging annoying exceptions that is excepted
		doNothing().when(stream).consumeContent();
		stream.close();
		verify(stream, times(1)).consumeContent();
	}

	@Test
	public void shouldConsumeAutomaticallyIfEOFWasNotReachedButStreamIsEmpty1() throws IOException {
		InputStream underlyingStream = new ReaderInputStream(new StringReader(""));
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		// avoids logging annoying exceptions that is excepted
		doNothing().when(stream).consumeContent();
		stream.close();
		verify(stream, times(1)).consumeContent();
	}

	@Test
	public void shouldConsumeAutomaticallyIfEOFWasNotReachedButStreamIsEmpty2() throws IOException {
		InputStream underlyingStream = new NullInputStream(0);
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		// avoids logging annoying exceptions that is excepted
		doNothing().when(stream).consumeContent();
		stream.close();
		verify(stream, times(1)).consumeContent();
	}

	@Test
	public void shouldNotConsumeAutomaticallyIfEOFWasReached() throws IOException {
		InputStream underlyingStream = new NullInputStream(0);
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		int readValue = stream.read();
		assertEquals(-1, readValue);
		stream.close();
		verify(stream, times(0)).consumeContent();
	}

	@Test
	public void shouldReadWithBufferReturnZeroWhenRequestedLengthIsZero() throws IOException {
		InputStream underlyingStream = new NullInputStream(0);
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		byte[] readBuffer = new byte[128];
		int readValue = stream.read(readBuffer, 0, 0);
		assertEquals(0, readValue);
		stream.close();
		verify(stream, times(1)).consumeContent();
	}

	@Test
	public void shouldReadWithBufferReturnMinuxOneWhenRequestedLengthStreamIsEOF() throws IOException {
		InputStream underlyingStream = new NullInputStream(0);
		HttpResponse httpResponse = null;
		ConnectionReleasingInputStream stream = spy(new ConnectionReleasingInputStream(underlyingStream, httpResponse));
		byte[] readBuffer = new byte[128];
		int readValue = stream.read(readBuffer, 0, 10);
		assertEquals(-1, readValue);
		stream.close();
		verify(stream, times(0)).consumeContent();
	}

}
