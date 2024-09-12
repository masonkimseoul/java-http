package com.techcourse.controller;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.UserException;
import com.techcourse.model.User;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;
import org.apache.coyote.http11.FileReader;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.request.Session;
import org.apache.coyote.http11.request.SessionManager;
import org.apache.coyote.http11.response.HttpResponse;
import org.apache.coyote.http11.response.HttpResponseBody;
import org.apache.coyote.http11.response.HttpResponseHeaders;
import org.apache.coyote.http11.response.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    public LoginController() {
    }

    public HttpResponse login(HttpRequest httpRequest) throws URISyntaxException, IOException {
        FileReader fileReader = FileReader.getInstance();
        String filePath = "/login";
        HttpStatusCode statusCode = HttpStatusCode.OK;
        String account = httpRequest.getRequestBodyValue("account");
        String password = httpRequest.getRequestBodyValue("password");
        try {
            User foundUser = InMemoryUserRepository.findByAccount(account)
                    .orElseThrow(() -> new UserException(account + "는 존재하지 않는 계정입니다."));
            if (foundUser.checkPassword(password)) {
                log.info("user : " + foundUser);
                statusCode = HttpStatusCode.FOUND;
                filePath = "/index.html";

                String jsessionid = UUID.randomUUID().toString();
                Session session = new Session(jsessionid);
                session.setAttribute("user", foundUser);
                SessionManager.add(session.getId(), session);
            }
        } catch (UserException e) {
            filePath = "/401.html";
            statusCode = HttpStatusCode.UNAUTHORIZED;
        }

        HttpResponseBody httpResponseBody = new HttpResponseBody(
                fileReader.readFile(filePath));
        HttpResponseHeaders httpResponseHeaders = new HttpResponseHeaders(new HashMap<>());
        httpResponseHeaders.setContentType(httpRequest);
        httpResponseHeaders.setContentLength(httpResponseBody);

        String jsessionid = httpRequest.getJSESSIONID();
        if (jsessionid.isEmpty()) {
            jsessionid = UUID.randomUUID().toString();
        }
        httpResponseHeaders.setCookie("JSESSION=" + jsessionid);
        return new HttpResponse(statusCode, httpResponseHeaders, httpResponseBody);
    }
}
