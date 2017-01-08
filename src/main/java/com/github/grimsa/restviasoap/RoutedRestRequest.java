package com.github.grimsa.restviasoap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpUtils;

import com.github.grimsa.restviasoap.generated.Request;

class RoutedRestRequest extends HttpServletRequestWrapper {

    private final String method;
    private final URI requestUri;
    private final String requestBody;
    private final Map<String, String[]> parameters;

    RoutedRestRequest(HttpServletRequest base, Request override) {
        super(base);
        this.method = override.getMethod().name();
        this.requestUri = override.getPath();
        this.requestBody = override.getValue();
        this.parameters = Collections.unmodifiableMap(new QueryParameterExtractor().toQueryParameters(requestUri));
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public int getContentLength() {
        return requestBody != null ? requestBody.length() : -1;
    }

    @Override
    public String getContentType() {
        return requestBody != null ? "application/json" : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public String getQueryString() {
        return requestUri.getRawQuery();
    }

    @Override
    public String getRequestURI() {
        String contextPath = getContextPath().isEmpty() ? "/" : getContextPath();
        return contextPath + requestUri.getRawPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return HttpUtils.getRequestURL(this);
    }

    @Override
    public String getServletPath() {
        // We do not know which servlet will handle this request, so act as if it was matched using "/*" pattern
        return "";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new PayloadServletInputStream(requestBody);
    }

    static class PayloadServletInputStream extends ServletInputStream {
        private static final byte[] EMPTY_ARRAY = new byte[0];

        private final ByteArrayInputStream payloadInputStream;

        PayloadServletInputStream(String payload) {
            byte[] payloadBytes = payload != null ? payload.getBytes(StandardCharsets.UTF_8) : EMPTY_ARRAY;
            payloadInputStream = new ByteArrayInputStream(payloadBytes);
        }

        @Override
        public int read() throws IOException {
            return payloadInputStream.read();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isFinished() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}