package com.github.grimsa.restviasoap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.junit.Test;

import com.github.grimsa.restviasoap.RoutedRestRequest.PayloadServletInputStream;
import com.github.grimsa.restviasoap.generated.HttpMethod;
import com.github.grimsa.restviasoap.generated.Request;

public class RoutedHttpServletRequestTest {

    @Test
    public void shouldOverrideRequestInfo() throws Exception {
        // given
        Request restRequestSpec = givenRestRequestSpecification(HttpMethod.GET, "/api/greeting?arg=val;jsessionid=TESTSESSION", null);
        HttpServletRequest soapRequest = givenSoapRequest(restRequestSpec);

        // when
        HttpServletRequest routedRequest = new RoutedRestRequest(soapRequest, restRequestSpec);

        // then
        assertEquals("/greeting-app", routedRequest.getContextPath());
        assertEquals(-1, routedRequest.getContentLength());
        assertNull(routedRequest.getContentType());
        assertEquals("UTF-8", routedRequest.getCharacterEncoding());
        assertEquals(DispatcherType.REQUEST, routedRequest.getDispatcherType());
        assertEquals("GET", routedRequest.getMethod());
        assertEquals(1, routedRequest.getParameterMap().size());
        assertArrayEquals(new String[] { "val;jsessionid=TESTSESSION" }, routedRequest.getParameterMap().get("arg"));
        assertNull(routedRequest.getPathInfo());
        assertNull(routedRequest.getPathTranslated());
        assertEquals("arg=val;jsessionid=TESTSESSION", routedRequest.getQueryString());
        assertEquals("/greeting-app/api/greeting", routedRequest.getRequestURI());
        assertEquals("http://localhost:8080/greeting-app/api/greeting", routedRequest.getRequestURL().toString());
        assertEquals("", routedRequest.getServletPath());
    }

    // based on: http://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl
    @Test
    public void shouldOverrideWithProperEscaping() throws Exception {
        // given
        Request restRequestSpec = givenRestRequestSpecification(HttpMethod.GET, "/test%3F/a%3F+b;jsessionid=S%3F+ID?p+1=c+d&p+2=e+f#a", null);
        HttpServletRequest soapRequest = givenSoapRequest(restRequestSpec);

        // when
        HttpServletRequest routedRequest = new RoutedRestRequest(soapRequest, restRequestSpec);

        // then
        assertEquals("/greeting-app", routedRequest.getContextPath());
        assertEquals(-1, routedRequest.getContentLength());
        assertNull(routedRequest.getContentType());
        assertEquals("UTF-8", routedRequest.getCharacterEncoding());
        assertEquals(DispatcherType.REQUEST, routedRequest.getDispatcherType());
        assertEquals("GET", routedRequest.getMethod());
        assertEquals(Arrays.asList("p 1", "p 2"), Collections.list(routedRequest.getParameterNames()));
        assertEquals(2, routedRequest.getParameterMap().size());
        assertArrayEquals(new String[] { "c d" }, routedRequest.getParameterMap().get("p 1"));
        assertArrayEquals(new String[] { "e f" }, routedRequest.getParameterMap().get("p 2"));
        assertNull(routedRequest.getPathInfo());
        assertNull(routedRequest.getPathTranslated());
        assertEquals("p+1=c+d&p+2=e+f", routedRequest.getQueryString());
        assertEquals("/greeting-app/test%3F/a%3F+b;jsessionid=S%3F+ID", routedRequest.getRequestURI());
        assertEquals("http://localhost:8080/greeting-app/test%3F/a%3F+b;jsessionid=S%3F+ID", routedRequest.getRequestURL().toString());
        assertEquals("", routedRequest.getServletPath());
    }

    private HttpServletRequest givenSoapRequest(Request restRequestSpec) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getContextPath()).willReturn("/greeting-app");
        given(request.getContentLength()).willReturn(275);
        given(request.getContentType()).willReturn("text/xml;charset=UTF-8");
        given(request.getCharacterEncoding()).willReturn("UTF-8");
        given(request.getDispatcherType()).willReturn(DispatcherType.REQUEST);
        given(request.getMethod()).willReturn("POST");
        given(request.getParameterMap()).willReturn(Collections.emptyMap());
        given(request.getPathInfo()).willReturn(null);
        given(request.getPathTranslated()).willReturn(null);
        given(request.getQueryString()).willReturn(null);
        given(request.getRequestURI()).willReturn("/greeting-app/soap-api");
        given(request.getRequestURL()).willReturn(new StringBuffer("http://localhost:8080/greeting-app/soap-api"));
        given(request.getServletPath()).willReturn("/soap-api");
        given(request.getScheme()).willReturn("http");
        given(request.getServerName()).willReturn("localhost");
        given(request.getServerPort()).willReturn(8080);

        String xml = toXml(restRequestSpec);
        given(request.getContentLength()).willReturn(xml.length());
        given(request.getInputStream()).willReturn(new PayloadServletInputStream(xml));

        return request;
    }

    private Request givenRestRequestSpecification(HttpMethod httpMethod, String path, String body) {
        Request req = new Request();
        req.setMethod(httpMethod);
        req.setPath(URI.create(path));
        req.setValue(body);
        return req;
    }

    private String toXml(Request restRequestSpec) throws Exception {
        Marshaller m = JAXBContext.newInstance(Request.class).createMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        m.marshal(restRequestSpec, out);

        InputStream serializedInnerContent = new ByteArrayInputStream(out.toByteArray());
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, serializedInnerContent);

        out.reset();
        soapMessage.writeTo(out);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
