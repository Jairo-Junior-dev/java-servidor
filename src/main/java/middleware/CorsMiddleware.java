package middleware;

import HTTP.RequestHttp;
import HTTP.ResponseHttp;

public class CorsMiddleware implements Middleware {
    @Override
    public void process(RequestHttp request, ResponseHttp response, MiddlewareChain chain) {

    }
}
