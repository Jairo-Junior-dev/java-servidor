package HTTP;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResponseHttp {
    private HttpStatus status;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private  HttpStatus httpStatus;
    public ResponseHttp() {}

    public ResponseHttp(HttpStatus status) {
        this.status = status;
    }

    public ResponseHttp withStatus(HttpStatus status) {
        this.status = status;
        return this;
    }
    public ResponseHttp withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }
    public ResponseHttp withBody(String body) {
        this.body = body;
        return this;
    }

    public String build() {
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (body == null) {
                body = "Erro interno: status HTTP não definido.";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").
                append(status.getStatusCode()).
                append(" ").
                append(status.getStatusDescription()).append("\r\n");

        headers.
                forEach((k, v) ->
                        sb.append(k).append(": ").
                                append(v).
                                append("\r\n"));

        if (body != null) {
            sb.append("Content-Length: ").
                    append(body.getBytes(StandardCharsets.UTF_8).length).
                    append("\r\n");
        }

        sb.append("\r\n");

        if (body != null) {
            sb.append(body);
        }

        return sb.toString();
    }

}
