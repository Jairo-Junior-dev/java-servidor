package HTTP;

import java.nio.charset.StandardCharsets;

public enum HttpStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    SERVICE_UNAVAILABLE(503,"Service Unavailable" ),
    UNAUTHORIZED(401,"Unauthorized"),
    INTERNAL_SERVER_ERROR(500,"Internal Server Error"),
    NO_CONTENT(204,"No Content");
    private final int statusCode;
    private final String statusDescription;


    HttpStatus(int statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
    public String getResponseLine() {
        return "HTTP/1.1 " + statusCode + " " + statusDescription + "\r\n\r\n";
    }
    public String getResponseLineWithBody(String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusDescription).append("\r\n");
        sb.append("Content-Type: text/plain; charset=utf-8\r\n");
        sb.append("Content-Length: ").append(body.getBytes(StandardCharsets.UTF_8).length).append("\r\n");
        sb.append("\r\n");
        sb.append(body);
        return sb.toString();
    }
    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

}
