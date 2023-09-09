package org.apache.catalina.controller;

import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

import java.io.IOException;

public class ResourceController extends HttpController {

    @Override
    public boolean canHandle(final HttpRequest httpRequest) {
        return "GET".equals(httpRequest.getMethod());
    }

    @Override
    protected void doGet(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        handleResource(httpRequest.getTarget(), httpRequest, httpResponse);
    }
}
