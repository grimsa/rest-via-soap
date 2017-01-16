package com.github.grimsa.restviasoap;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestViaSoapIT {

    private static Server server;
    private static int serverPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new Server();
        ServerConnector c = new ServerConnector(server);
        c.setHost("localhost");
        c.setPort(0);
        ServletContextHandler handler = new ServletContextHandler(server, "/app", false, false);
        ServletHolder servletHolder = new ServletHolder(TestServlet.class);
        handler.addServlet(servletHolder, "/api/user/*");
        server.addConnector(c);
        server.start();
        serverPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    private String getBaseAddress() {
        return "http://localhost:" + serverPort + "/app";
    }

    @Test
    public void testName() {
        when().get(getBaseAddress() + "/api/user/100").then().body(equalTo("Hello"));
    }

}
