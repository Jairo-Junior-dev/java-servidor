package HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestHttp {
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;
    private final String body;
    private Map<String, String> queryParams = new HashMap<>();

    public RequestHttp(String method, String path, String protocol, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
    }

    public static RequestHttp parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Requisição vazia");
        }

        String[] requestParts = requestLine.split(" ");
        /***
         * POST /api/usuarios HTTP/1.1
         *  Post :requestParts[0];
         *  Path => /api/usuarios::requestParts[1];
         *  Protocol HTTP/1.1 : requestParts[2]
         *
         * Host: exemplo.com
         * Content-Type: application/json
         * Content-Length: 48
         *
         * {
         *   "nome": "Jairo",
         *   "email": "jairo@email.com"
         * }
         */
        String method = requestParts[0];
        String rawPath = requestParts.length > 1 ? requestParts[1] : "";
        String protocol = requestParts.length > 2 ? requestParts[2] : "";
        String cleanPath = rawPath;

        Map<String, String> queryParams = new HashMap<>();
        //Path => /api/usuarios::requestParts[1]; Ele vai conferir o index do '?';
        int queryIndex = rawPath.indexOf('?');
        // Se ele tiver na ultima posição
        if (queryIndex != -1) {
            // o clean Path pega o valor da substring
            cleanPath = rawPath.substring(0, queryIndex);
            String queryString = rawPath.substring(queryIndex + 1);

            for (String param : queryString.split("&")) {

                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    queryParams.put(pair[0], pair[1]);
                } else if (pair.length == 1) {
                    queryParams.put(pair[0], "");
                }
            }
        }
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx != -1) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(key, value);
            }
        }
        StringBuilder body = new StringBuilder();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            int contentLength = 0;
            if (headers.containsKey("Content-Length")) {
                try {
                    contentLength = Integer.parseInt(headers.get("Content-Length"));
                } catch (NumberFormatException ignored) {}
            }

            for (int i = 0; i < contentLength; i++) {
                int ch = in.read();
                if (ch != -1) {
                    body.append((char) ch);
                }
            }
        }
        RequestHttp request = new RequestHttp(method, cleanPath, protocol, headers, body.toString());
        request.queryParams = queryParams;
        return request;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.getOrDefault(key, "");
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}
