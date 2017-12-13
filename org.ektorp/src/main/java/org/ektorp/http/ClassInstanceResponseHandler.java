package org.ektorp.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class ClassInstanceResponseHandler extends StdResponseHandler {

    /**
     * this field can be set externally. it is not final on purpose.
     * in case we use a ThreadLocal to make sure there is only one user of the ClassInstanceResponseHandler instance, we can use the setter to change the state.
     */
    private Class clazz;

    private ObjectMapper objectMapper;
	
	private Charset charset = Charset.forName("UTF-8");

    public ClassInstanceResponseHandler(ObjectMapper objectMapper) {
        this.setObjectMapper(objectMapper);
    }

    public ClassInstanceResponseHandler(ObjectMapper objectMapper, Class clazz) {
        this.setObjectMapper(objectMapper);
        this.setClazz(clazz);
    }

    /**
     * this method is overridden in order to make sure we use our ObjectMapper instance.<br>
     * this is need in order to make sure we have less contention inside Jackson lib that makes use of synchronization.<br>
     */
    @Override
    public Object success(HttpResponse hr) throws IOException {
        InputStream content = null;
        Reader contentReader = null;
        try {
            content = hr.getContent();
            // providing the charset explicitly will save Jackson from having to auto detect the charset
            // that charset will most likely be the same for every document in a given database
            contentReader = new InputStreamReader(content, charset);
            return getObjectMapper().readValue(contentReader, getClazz());
        } finally {
            IOUtils.closeQuietly(contentReader);
            IOUtils.closeQuietly(content);
        }
    }

    /**
     * this method is overridden in order to make sure we use our ObjectMapper instance.<br>
     * this is need in order to make sure we have less contention inside Jackson lib that makes use of synchronization.<br>
     */
    @Override
    public Object error(HttpResponse hr) {
        throw createDbAccessException(hr, getObjectMapper());
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
