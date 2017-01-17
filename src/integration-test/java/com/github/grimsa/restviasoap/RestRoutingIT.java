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
import com.github.grimsa.restviasoap.generated.Response;
import com.google.common.io.Resources;

import io.restassured.RestAssured;

public class RestRoutingIT {

    private static Server server;
    private static int serverPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new Server(0);
        ServletContextHandler handler = new ServletContextHandler(server, "/app", false, false);

        handler.addFilter(RestRoutingFilter.class, "/soap-api", EnumSet.of(DispatcherType.REQUEST));
        handler.addServlet(TestServlet.class, "/api/user/*");

        server.start();
        serverPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        RestAssured.baseURI = "http://localhost:" + serverPort + "/app";
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    @Test
    public void shouldWriteResponseToWriter() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_WRITES_TO_WRITER, null);

        // when
        String soapResponse = whenSoapMessageIsSent(soapRequest);

        // response
        thenResponseIs(soapResponse, HttpStatus.OK_200, TestServlet.RESPONSE_BODY);
    }

    @Test
    public void shouldWriteResponseToStream() throws IOException {
        // given
        String soapRequest = givenSoapRequest(HttpMethod.GET, TestServlet.PATH_REQUEST_WRITES_TO_STREAM, null);

        // when
        String soapResponse = whenSoapMessageIsSent(soapRequest);

        // response
        thenResponseIs(soapResponse, HttpStatus.OK_200, TestServlet.RESPONSE_BODY);
    }

    private String givenSoapRequest(HttpMethod httpMethod, String path, String body) throws IOException {
        String template = Resources.toString(Resources.getResource("templates/request.xml"), StandardCharsets.UTF_8);
        return template.replace("${method}", httpMethod.name()).replace("${path}", path).replace("${body}", Optional.ofNullable(body).orElse(""));
    }

    private String whenSoapMessageIsSent(String soapRequest) {
        return given().request().contentType("application/soap+xml; charset=UTF-8;").body(soapRequest).post("/soap-api").andReturn().asString();
    }

    private void thenResponseIs(String soapResponse, int httpStatus, String body) {
        Response restResponse = extractRestResponse(soapResponse);
        assertEquals(HttpStatus.OK_200, restResponse.getStatus());
        assertEquals(body, restResponse.getValue());
    }

    private Response extractRestResponse(String soapResponse) {
        try {
            ByteArrayInputStream soapResponseIs = new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8));
            SOAPMessage message = MessageFactory.newInstance().createMessage(null, soapResponseIs);
            Unmarshaller unmarshaller = JAXBContext.newInstance(Response.class).createUnmarshaller();
            return (Response) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
        } catch (JAXBException | SOAPException | IOException e) {
            throw new AssertionError("Failed to extract Response from SOAP message: " + soapResponse, e);
        }
    }

}
