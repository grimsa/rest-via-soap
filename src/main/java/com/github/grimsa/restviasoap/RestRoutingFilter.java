package com.github.grimsa.restviasoap;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.github.grimsa.restviasoap.generated.Request;

public class RestRoutingFilter implements Filter {

    private SoapEnvelopeHelper soapEnvelopeHelper = new SoapEnvelopeHelper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Request restRequestSpecification = soapEnvelopeHelper.unwrap(request.getInputStream());

        // TODO: build wrapped request

        request.getRequestDispatcher(restRequestSpecification.getPath().toASCIIString()).forward(request, response);
    }

    @Override
    public void destroy() {
    }
}
