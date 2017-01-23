package com.github.grimsa.restviasoap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.grimsa.restviasoap.generated.Response;

class RoutedRestResponse extends HttpServletResponseWrapper {

    private final CapturingServletOutputStream outputStream;
    private final PrintWriter writer;

    RoutedRestResponse(HttpServletResponse response) {
        super(response);
        outputStream = new CapturingServletOutputStream();
        writer = new PrintWriter(outputStream, true);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    OutputStream getResponseOutputStream() throws IOException {
        return super.getOutputStream();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new ResponseWrappingException("Redirects are not supported. Requested redirect location: " + location);
    }

    @Override
    public void sendError(int sc) throws IOException {
        resetBuffer();
        setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        resetBuffer();
        setStatus(sc);
    }

    Response toResponse() {
        Response response = new Response();
        response.setStatus(getStatus());
        response.setValue(new String(outputStream.getCapturedBytes(), StandardCharsets.UTF_8));
        return response;
    }

    private static class CapturingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            capturedOutput.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            capturedOutput.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            capturedOutput.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException("Not implemented");
        }

        private byte[] getCapturedBytes() {
            return capturedOutput.toByteArray();
        }
    }

    static class ResponseWrappingException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ResponseWrappingException(String string) {
            super(string);
        }
    }
}