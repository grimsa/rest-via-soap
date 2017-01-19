package com.github.grimsa.restviasoap;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.github.grimsa.restviasoap.generated.HttpMethod;
import com.github.grimsa.restviasoap.generated.Request;
import com.github.grimsa.restviasoap.generated.Response;

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

    @Test
    public void shouldWrapRestResponseIntoSoapMessage() {
        // given
        int responseStatus = 200;
        String responseJson = "{ 'key' : 'value' }";

        StringBuilder sb = new StringBuilder();
        sb.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        sb.append("<SOAP-ENV:Header/>");
        sb.append("<SOAP-ENV:Body>");
        sb.append("<response xmlns=\"http://g.rimsa.lt/rest-over-soap/\" status=\""+ responseStatus + "\">" + responseJson + "</response>");
        sb.append("</SOAP-ENV:Body>");
        sb.append("</SOAP-ENV:Envelope>");
        String expectedSoapMessage = sb.toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Response response = new Response();
        response.setStatus(responseStatus);
        response.setValue(responseJson);

        // when
        soapEnvelopeHelper.writeResponse(response, outputStream);

        // then
        String soapMessage = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(expectedSoapMessage, soapMessage);
    }
}
