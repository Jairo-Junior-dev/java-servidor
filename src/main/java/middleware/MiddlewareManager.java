package middleware;

import HTTP.RequestHttp;
import HTTP.ResponseHttp;

import java.util.List;

public class MiddlewareManager implements  MiddlewareChain{
    private final List<Middleware>middlewares;
    private int middlewareIndex = 0;
    public MiddlewareManager(List<Middleware> middlewares){
        this.middlewares = middlewares;
    }

    @Override
    public void next(RequestHttp request, ResponseHttp response) {
            if (middlewareIndex<middlewares.size()){
                Middleware curr = middlewares.get(middlewareIndex);
                middlewareIndex++;
                curr.process(request,response,this);
            }
    }
}
