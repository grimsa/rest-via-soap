package com.github.grimsa.restviasoap;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.grimsa.restviasoap.RoutedRestResponse.ResponseWrappingException;
import com.github.grimsa.restviasoap.generated.Request;
import com.github.grimsa.restviasoap.generated.Response;

public class RestRoutingFilter implements Filter {

    private SoapMessageHelper soapMessageHelper = new SoapMessageHelper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        Request restRequestSpecification = soapMessageHelper.readRequest(request.getInputStream());

        RoutedRestRequest routedRequest = new RoutedRestRequest((HttpServletRequest) request, restRequestSpecification);
        RoutedRestResponse routedResponse = new RoutedRestResponse((HttpServletResponse) response);

        try {
            request.getRequestDispatcher(restRequestSpecification.getPath().getRawPath()).forward(routedRequest, routedResponse);

        } catch (ResponseWrappingException routingFilterException) {
            throw routingFilterException;
        }

        transformResponse(soapMessageHelper, routedResponse);
        routedResponse.flushBuffer();
    }

    void transformResponse(SoapMessageHelper soapMessageHelper, RoutedRestResponse routedRestResponse) throws IOException {
        Response response = routedRestResponse.toResponse();

        routedRestResponse.setStatus(200);
        routedRestResponse.setContentType("application/xml;charset=UTF-8");
        soapMessageHelper.writeResponse(response, routedRestResponse.getResponseOutputStream());
    }

    @Override
    public void destroy() {
    }
}
