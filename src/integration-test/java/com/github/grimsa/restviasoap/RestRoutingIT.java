package com.github.grimsa.restviasoap;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void testGetRequest() throws IOException {
        // given
        String soapRequest = Resources.toString(Resources.getResource("requests/GET.xml"), StandardCharsets.UTF_8);

        // when
        String soapResponse = given().request().contentType("application/soap+xml; charset=UTF-8;").body(soapRequest).post("/soap-api").andReturn().asString();

        // response
        assertTrue(soapResponse.contains("status=\"200\""));
    }

}
