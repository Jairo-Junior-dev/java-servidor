
import Config.ConfigLoader;
import Config.Looger;
import HTTP.HttpStatus;
import HTTP.RequestHttp;
import HTTP.ResponseHttp;
import middleware.AuthMiddleware;
import middleware.CorsMiddleware;
import middleware.MiddlewareManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class   Main {
    private static final Map<String, HttpStatus> routes = new HashMap<>();
    private static ConfigLoader loader = ConfigLoader.getInstance();
    private static MiddlewareManager manager = new MiddlewareManager(List.of(new AuthMiddleware() ,
            new CorsMiddleware()
    ));
    static {
        routes.put(Route.ROOT.getPath(), HttpStatus.OK);
        routes.put(Route.INDEX.getPath(), HttpStatus.OK);
    }

    public static void main(String[] args) {
        Looger log = Looger.getInstance();
        MiddlewareManager middlewareManager = new MiddlewareManager(Arrays.asList(new AuthMiddleware(),new CorsMiddleware()));
        log.setLevel("DEBUG");

        log.info("Servidor iniciado :");

        try (ServerSocket serverSocket = new ServerSocket(loader.getIntProperty("server.port",4000))) {
            serverSocket.setReuseAddress(true);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    String clientIp = clientSocket.getInetAddress().getHostAddress();
                    log.info("Conexão aceita de: " + clientIp);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

                    OutputStream out = clientSocket.getOutputStream();

                    try {
                        manager.next(RequestHttp.parseRequest(in),);
                    }catch (IOException exception){
                        log.error("Erro ",(Exception) exception);
                    }


                    String requestLine = in.readLine();
                    log.debug("Request: " + requestLine);

                    if (requestLine == null || requestLine.isEmpty()) {
                        log.warn("Request vazio ou nulo de: " + clientIp);
                        continue;
                    }
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        log.debug("Header: " + line);
                    }
                    String response =  processRequest(requestLine, log);
                    RequestHttp requestHttp = new RequestHttp();

                    ResponseHttp responseHttp = new ResponseHttp();
                    log.info("Resposta enviada para " + clientIp + ": " + response.trim());
                    middlewareManager.next();
                    out.write(response.getBytes(StandardCharsets.UTF_8));
                    out.flush();

                } catch (IOException e) {
                    log.error("Erro na conexão com cliente.", e);
                }
            }
        } catch (IOException e) {
            log.error("Erro no servidor.", e);
        }
    }

    private static String processRequest(String requestLine, Looger log) {
        try {
            boolean maintenanceEnable = loader.getBooleanProperty("maintenance.enable", false);
            if (maintenanceEnable) {
                String maintenanceMsg = loader.getProperty("maintenance.message",
                        "Servidor em manutenção. Tente novamente mais tarde.");
                log.info("Modo manutenção ativo. Retornando 503");
                return HttpStatus.SERVICE_UNAVAILABLE.getResponseLineWithBody(maintenanceMsg);
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                log.warn("Formato inválido da requisição: " + requestLine);
                return HttpStatus.BAD_REQUEST.getResponseLine();
            }

            String method = parts[0];
            String path = parts[1];

            log.debug("Método: " + method + " | Path: " + path);

            if (!method.equals("GET")) {
                return HttpStatus.METHOD_NOT_ALLOWED.getResponseLine();
            }

            HttpStatus status = routes.getOrDefault(path, HttpStatus.NOT_FOUND);
            return status.getResponseLine();

        } catch (Exception e) {
            log.error("Erro ao processar requisição: " + requestLine, e);
            return HttpStatus.METHOD_NOT_ALLOWED.getResponseLine();
        }
    }

}
