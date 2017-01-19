package com.github.grimsa.restviasoap;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Optional;

import javax.servlet.DispatcherType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.grimsa.restviasoap.generated.HttpMethod;
import com.google.common.io.Resources;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class RestRoutingIT {

    private static Server server;
    private static int serverPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new Server(0);
        ServletContextHandler handler = new ServletContextHandler(server, "/app", false, false);

        handler.addFilter(RestRoutingFilter.class, "/soap-api", EnumSet.of(DispatcherType.REQUEST));
        handler.addServlet(TestServlet.class, TestServlet.SERVLET_PATH);

        server.start();
        serverPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        RestAssured.baseURI = "http://localhost:" + serverPort + "/app";
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    @Test
    public void shouldHandleOutputToWriter() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_WRITES_TO_WRITER, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.OK_200, TestServlet.RESPONSE_BODY);
    }

    @Test
    public void shouldHandleOutputToStream() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_WRITES_TO_STREAM, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.OK_200, TestServlet.RESPONSE_BODY);
    }

    @Test
    public void shouldHandleNoOutput() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.POST, TestServlet.PATH_REQUEST_EMPTY_BODY, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.OK_200, "");
    }

    @Test
    public void shouldHandleSendRedirect() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_SENDS_REDIRECT, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, httpResponse.getStatusCode());
    }

    @Test
    public void shouldHandleSendError() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_SENDS_ERROR, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.NOT_FOUND_404, "");
    }

    @Test
    public void shouldHandleSendErrorWithMessage() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_SENDS_ERROR_WITH_MESSAGE, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.NOT_FOUND_404, "");
    }

    @Test
    public void shouldHandleApplicationExceptions() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_THROWS_EXCEPTION, null);

        // when
        Response httpResponse = whenSoapMessageIsSent(soapRequest);

        // then
        String soapResponse = thenSoapResponseIsReceived(httpResponse);
        thenResponseIs(soapResponse, HttpStatus.INTERNAL_SERVER_ERROR_500, "");
    }

    private String givenSoapRequest(HttpMethod httpMethod, String path, String body) throws IOException {
        String template = Resources.toString(Resources.getResource("templates/request.xml"), StandardCharsets.UTF_8);
        return template.replace("${method}", httpMethod.name()).replace("${path}", path).replace("${body}", Optional.ofNullable(body).orElse(""));
    }

    private Response whenSoapMessageIsSent(String soapRequest) {
        return given().request().contentType("application/soap+xml; charset=UTF-8;").body(soapRequest).post("/soap-api");
    }

    private String thenSoapResponseIsReceived(Response response) {
        String responseAsString = response.asString();
        response.then().statusCode(200).contentType(ContentType.XML);
        return responseAsString;
    }

    private void thenResponseIs(String soapResponse, int httpStatus, String body) {
        com.github.grimsa.restviasoap.generated.Response restResponse = extractRestResponse(soapResponse);
        assertEquals(httpStatus, restResponse.getStatus());
        assertEquals(body, restResponse.getValue());
    }

    private com.github.grimsa.restviasoap.generated.Response extractRestResponse(String soapResponse) {
        try {
            ByteArrayInputStream soapResponseIs = new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8));
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, soapResponseIs);
            Unmarshaller unmarshaller = JAXBContext.newInstance(com.github.grimsa.restviasoap.generated.Response.class).createUnmarshaller();
            return (com.github.grimsa.restviasoap.generated.Response) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
        } catch (JAXBException | SOAPException | IOException e) {
            throw new AssertionError("Failed to extract Response from SOAP message: " + soapResponse, e);
        }
    }

}
