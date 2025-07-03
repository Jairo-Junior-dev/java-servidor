package middleware;

import HTTP.RequestHttp;
import HTTP.ResponseHttp;

public interface MiddlewareChain {
    void next(RequestHttp request, ResponseHttp response);
}
