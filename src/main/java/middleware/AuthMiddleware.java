package middleware;

import Config.ConfigLoader;
import Config.Looger;
import HTTP.HttpStatus;
import HTTP.RequestHttp;
import HTTP.ResponseHttp;

public class AuthMiddleware implements Middleware {
    private Looger looger = Looger.getInstance();
    private ConfigLoader loader = ConfigLoader.getInstance();

    @Override
    public void process(RequestHttp request, ResponseHttp response, MiddlewareChain chain) {
        boolean enableAuth = loader.getBooleanProperty("security.enableAuth", false);
        if (!enableAuth) {
            looger.info("Autenticação desabilitada.");
            chain.next(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            looger.error("Acesso negado. Token de autenticação não fornecido.");
            response.withStatus(HttpStatus.UNAUTHORIZED).withBody("Acesso negado: token não fornecido.");
            return;
        }
        String validToken = "Bearer meu-token-secreto";
        if (!authHeader.equals(validToken)) {
            looger.error("Acesso negado. Token inválido.");
            response.withStatus(HttpStatus.UNAUTHORIZED).withBody("Acesso negado: token inválido.");
            return;
        }
        looger.info("Autenticação bem-sucedida.");
        chain.next(request, response);
    }
}
