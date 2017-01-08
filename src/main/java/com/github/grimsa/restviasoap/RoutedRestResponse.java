package com.github.grimsa.restviasoap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.grimsa.restviasoap.generated.Response;

class RoutedRestResponse extends HttpServletResponseWrapper {

    private final CapturingServletOutpuStream outputStream;
    private final PrintWriter writer;

    RoutedRestResponse(HttpServletResponse response) {
        super(response);
        outputStream = new CapturingServletOutpuStream();
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

    void transformResponse() throws IOException {
        if (getContentType().startsWith("application/json")) {
            // return JSON wrapped in SOAP
            Response response = new Response();
            response.setStatus(getStatus());
            response.setValue(new String(outputStream.getCapturedBytes(), StandardCharsets.UTF_8));

            setStatus(200);
            setContentType("application/xml;charset=UTF-8");
            new SoapMessageHelper().writeResponse(response, super.getOutputStream());
        } else {
            // return same output, no changes
            byte[] capturedOutput = outputStream.getCapturedBytes();
            super.getOutputStream().write(capturedOutput);
        }
    }

    private static class CapturingServletOutpuStream extends ServletOutputStream {
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
}