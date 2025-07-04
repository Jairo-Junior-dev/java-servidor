package middleware;

import Config.Looger;
import HTTP.RequestHttp;
import HTTP.ResponseHttp;

import java.util.List;

public class MiddlewareManager implements  MiddlewareChain{
    private final List<Middleware>middlewares;
    private Looger looger ;
    private int middlewareIndex = 0;
    public MiddlewareManager(List<Middleware> middlewares){this.middlewares = middlewares; looger= Looger.getInstance();}

    @Override
    public void next(RequestHttp request, ResponseHttp response) {
            if (middlewareIndex<middlewares.size()){

                Middleware curr = middlewares.get(middlewareIndex);
                looger.info("Log Atual : " + curr.getClass().getSimpleName());
                middlewareIndex++;
                curr.process(request,response,this);
            }
    }
}
