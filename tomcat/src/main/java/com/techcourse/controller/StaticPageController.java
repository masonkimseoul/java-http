package com.techcourse.controller;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.model.User;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.coyote.http11.FileReader;
import org.apache.coyote.http11.request.HttpCookie;
import org.apache.coyote.http11.request.HttpCookieExtractor;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.request.HttpRequestHeaders;
import org.apache.coyote.http11.request.SessionManager;
import org.apache.coyote.http11.response.HttpResponse;
import org.apache.coyote.http11.response.HttpResponseBody;
import org.apache.coyote.http11.response.HttpResponseHeaders;
import org.apache.coyote.http11.response.HttpStatusCode;

public class StaticPageController {

    public StaticPageController() {
    }

    public HttpResponse getStaticPage(HttpRequest httpRequest) throws URISyntaxException, IOException {
        FileReader fileReader = FileReader.getInstance();
        HttpStatusCode statusCode = HttpStatusCode.OK;

        if (httpRequest.getHttpRequestPath().equals("/login") && checkLogin(httpRequest)) {
            statusCode = HttpStatusCode.FOUND;
            httpRequest.setHttpRequestPath("/index.html");
        }

        HttpResponseBody httpResponseBody = new HttpResponseBody(
            fileReader.readFile(httpRequest.getHttpRequestPath()));
        HttpResponseHeaders httpResponseHeaders = new HttpResponseHeaders(new HashMap<>());
        httpResponseHeaders.setContentType(httpRequest);
        httpResponseHeaders.setContentLength(httpResponseBody);
        return new HttpResponse(statusCode, httpResponseHeaders, httpResponseBody);
    }

    private boolean checkLogin(HttpRequest request) {
        String jsessionid = request.getJSESSIONID();
        if (jsessionid.isEmpty()) {
            return false;
        }
        return SessionManager.containsSession(jsessionid);
    }
}
