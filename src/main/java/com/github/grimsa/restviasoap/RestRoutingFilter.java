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

import com.github.grimsa.restviasoap.generated.Request;

public class RestRoutingFilter implements Filter {

    private SoapMessageHelper soapEnvelopeHelper = new SoapMessageHelper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Request restRequestSpecification = soapEnvelopeHelper.readRequest(request.getInputStream());

        RoutedRestRequest routedRestRequest = new RoutedRestRequest((HttpServletRequest) request, restRequestSpecification);
        RoutedRestResponse routedRestResponse = new RoutedRestResponse((HttpServletResponse) response);

        request.getRequestDispatcher(restRequestSpecification.getPath().getRawPath()).forward(routedRestRequest, routedRestResponse);

        routedRestResponse.transformResponse();
    }

    @Override
    public void destroy() {
    }
}
