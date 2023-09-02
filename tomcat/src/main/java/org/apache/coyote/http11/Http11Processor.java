package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String requestUri = reader.readLine().split(" ")[1];
            log.info("requestUri = {}", requestUri);

            final Map<String, String> request = new HashMap<>();
            String line;
            while (!"".equals(line = reader.readLine())) {
                String[] value = line.split(": ");
                request.put(value[0], value[1]);
            }
            log.info("request = {}", request);

            var statusCode = "404";
            var contentType = "text/html;charset=utf-8";
            final URL resource404 = getClass().getClassLoader().getResource("static/404.html");
            var responseBody = new String(Files.readAllBytes(new File(resource404.getFile()).toPath()));

            if (requestUri.equals("/")) {
                statusCode = "200 OK";
                contentType = "text/html;charset=utf-8";
                responseBody = "Hello world!";

                final var response = String.join("\r\n",
                        "HTTP/1.1 " + statusCode + " ",
                        "Content-Type: " + contentType + " ",
                        "Content-Length: " + responseBody.getBytes().length + " ",
                        "",
                        responseBody);
                outputStream.write(response.getBytes());
                outputStream.flush();
                return;
            }

            if (requestUri.contains("/login?")) {
                statusCode = "302";
                contentType = "text/html;charset=utf-8";
                var location = "";

                int index = requestUri.indexOf("?");
                String queryString = requestUri.substring(index + 1);

                Map<String, String> queries = new HashMap<>();
                for (String query : queryString.split("&")) {
                    queries.put(query.split("=")[0], query.split("=")[1]);
                    Optional<User> user = InMemoryUserRepository.findByAccount(queries.get("account"));

                    if (user.isPresent() && user.get().checkPassword(queries.get("password"))) {
                        location = "/index.html";
                        log.info("user: {}", user.get());
                    } else {
                        location = "/401.html";
                    }
                }
                final var response = String.join("\r\n",
                        "HTTP/1.1 " + statusCode + " ",
                        "Content-Type: " + contentType + " ",
                        "Location: " + location + " ",
                        "");

                outputStream.write(response.getBytes());
                outputStream.flush();
                return ;
            }

            if (requestUri.equals("/login")) {
                requestUri = "login.html";
            }

            final String fileName = "static/" + requestUri;
            final URL resource = getClass().getClassLoader().getResource(fileName);
            if (request.get("Accept") != null) {
                contentType = request.get("Accept").split(",")[0];
            }
            if (resource != null) {
                statusCode = "200 OK";
                responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
            }

            final var response = String.join("\r\n",
                    "HTTP/1.1 " + statusCode + " ",
                    "Content-Type: " + contentType + " ",
                    "Content-Length: " + responseBody.getBytes().length + " ",
                    "",
                    responseBody);
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }
}
