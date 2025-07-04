package middleware;

import HTTP.RequestHttp;
import HTTP.ResponseHttp;

public interface Middleware {
    void process(RequestHttp request, ResponseHttp response,MiddlewareChain chain);
}
