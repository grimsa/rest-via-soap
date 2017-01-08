package com.github.grimsa.restviasoap;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.github.grimsa.restviasoap.generated.HttpMethod;
import com.github.grimsa.restviasoap.generated.Request;

public class SoapMessageHelperTest {

    private SoapMessageHelper soapEnvelopeHelper = new SoapMessageHelper();

    @Test
    public void shouldUnwrapRestRequestFromSoapMessage() {
        // given
        StringBuilder sb = new StringBuilder();
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        sb.append("  <soapenv:Header />");
        sb.append("  <soapenv:Body>");
        sb.append("    <request xmlns=\"http://g.rimsa.lt/rest-over-soap/\" method=\"POST\" path=\"/api/ačiū\">content</request>");
        sb.append("  </soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        String example = sb.toString();

        InputStream is = new ByteArrayInputStream(example.getBytes(StandardCharsets.UTF_8));

        // when
        Request rest = soapEnvelopeHelper.readRequest(is);

        // then
        assertEquals(HttpMethod.POST, rest.getMethod());
        assertEquals("/api/ačiū", rest.getPath().toString());
        assertEquals("content", rest.getValue());
    }
}
