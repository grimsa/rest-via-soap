package com.github.grimsa.restviasoap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static final String PATH_REQUEST_WRITES_TO_WRITER = "/api/user/200/1";
    static final String PATH_REQUEST_WRITES_TO_STREAM = "/api/user/200/2";
    static final String PATH_REQUEST_EMPTY_BODY = "/api/user/200/3";
    static final String PATH_REQUEST_SENDS_ERROR = "/api/user/404";
    static final String PATH_REQUEST_SENDS_ERROR_WITH_MESSAGE = "/api/user/404/msg";
    static final String PATH_REQUEST_SENDS_REDIRECT = "/api/user/302";
    static final String PATH_REQUEST_THROWS_EXCEPTION = "/api/user/500";
    static final String RESPONSE_BODY = "{ 'id' : '000', 'name' : 'Bob' }";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch(req.getRequestURI().substring(req.getContextPath().length())) {
        case PATH_REQUEST_WRITES_TO_WRITER:
            resp.getWriter().print(RESPONSE_BODY);
            resp.getWriter().close();
            break;

        case PATH_REQUEST_WRITES_TO_STREAM:
            resp.getOutputStream().print(RESPONSE_BODY);
            break;

        case PATH_REQUEST_SENDS_ERROR:
            resp.sendError(404);
            break;

        case PATH_REQUEST_SENDS_ERROR_WITH_MESSAGE:
            resp.sendError(404, "Boom, not found!");
            break;

        case PATH_REQUEST_SENDS_REDIRECT:
            resp.sendRedirect(PATH_REQUEST_WRITES_TO_STREAM);
            break;

        default:
            throw new IllegalStateException("Something failed in application logic!");
        }
    }

}
