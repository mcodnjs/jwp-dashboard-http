package org.apache.coyote.http11.request;

import org.apache.coyote.http11.common.HttpCookie;
import org.apache.coyote.http11.common.HttpHeaders;

import java.util.Map;

public class HttpRequest {

    private final HttpRequestLine requestLine;
    private final HttpHeaders headers;
    private final Map<String, String> body;

    private HttpRequest(final HttpRequestLine requestLine, final HttpHeaders headers, final Map<String, String> body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest from(final HttpRequestLine requestLine, final HttpHeaders header, final Map<String, String> body) {
        return new HttpRequest(requestLine, header, body);
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getTarget() {
        return requestLine.getTarget();
    }

    public String getVersion() {
        return requestLine.getVersion();
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpCookie getCookie() {
        return headers.getCookie();
    }

    public Map<String, String> getBody() {
        return body;
    }
}
