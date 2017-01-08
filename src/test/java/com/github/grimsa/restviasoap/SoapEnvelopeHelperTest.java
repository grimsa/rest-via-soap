package com.github.grimsa.restviasoap;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.github.grimsa.restviasoap.generated.HttpMethod;
import com.github.grimsa.restviasoap.generated.Request;

public class SoapEnvelopeHelperTest {

    private SoapEnvelopeHelper soapEnvelopeHelper = new SoapEnvelopeHelper();

    @Test
    public void shouldUnwrapRequestFromEnvelope() {
        // given
        StringBuilder sb = new StringBuilder();
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        sb.append("  <soapenv:Header />");
        sb.append("  <soapenv:Body>");
        sb.append("    <request xmlns=\"http://g.rimsa.lt/rest-over-soap/\" method=\"GET\" path=\"api/ačiū\">content</request>");
        sb.append("  </soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        String example = sb.toString();

        InputStream is = new ByteArrayInputStream(example.getBytes(StandardCharsets.UTF_8));

        // when
        Request rest = soapEnvelopeHelper.unwrap(is);

        // then
        assertEquals(HttpMethod.GET, rest.getMethod());
        assertEquals("api/ačiū", rest.getPath().toString());
        assertEquals("content", rest.getValue());
    }
}
