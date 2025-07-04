package middleware;

import Config.ConfigLoader;
import HTTP.RequestHttp;
import HTTP.ResponseHttp;

public class CorsMiddleware implements Middleware {
    private final ConfigLoader config = ConfigLoader.getInstance();

    @Override
    public void process(RequestHttp request, ResponseHttp response, MiddlewareChain chain) {
        if (config.getBooleanProperty("security.enableCors", true)) {
            String allowedOrigins = config.getProperty("security.allowedOrigins", "*");
            String allowedMethods = config.getProperty("security.allowMethods", "GET,POST,OPTIONS");
            String allowedHeaders = config.getProperty("security.allowedHeaders", "Content-Type");
            response.withHeader("Access-Control-Allow-Origin", allowedOrigins);
            response.withHeader("Access-Control-Allow-Methods", allowedMethods);
            response.withHeader("Access-Control-Allow-Headers", allowedHeaders);
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.withStatus(HTTP.HttpStatus.NO_CONTENT).withBody("");
            return;
        }
        chain.next(request, response);
    }
}
