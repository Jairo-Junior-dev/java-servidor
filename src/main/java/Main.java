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
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class Main {
        private static final Map<String, HttpStatus> routes = new HashMap<>();
        private static final ConfigLoader loader = ConfigLoader.getInstance();
        private static final MiddlewareManager manager = new MiddlewareManager(List.of(
                new AuthMiddleware(),
                new CorsMiddleware()
        ));

        static {
            routes.put(Route.ROOT.getPath(), HttpStatus.OK);
            routes.put(Route.INDEX.getPath(), HttpStatus.OK);
            routes.put("/hello", HttpStatus.OK);
        }

        public static void main(String[] args) {
            Looger log = Looger.getInstance();
            log.setLevel("DEBUG");
            log.info("Servidor iniciado:");

            try (ServerSocket serverSocket = new ServerSocket(loader.getIntProperty("server.port", 4000))) {
                serverSocket.setReuseAddress(true);

                while (true) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        String clientIp = clientSocket.getInetAddress().getHostAddress();
                        log.info("Conexão aceita de: " + clientIp);

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                        OutputStream out = clientSocket.getOutputStream();

                        try {
                            RequestHttp requestHttp = RequestHttp.parseRequest(in);
                            log.debug("Request: " + requestHttp.getMethod() + " " + requestHttp.getPath());
                            ResponseHttp responseHttp = new ResponseHttp();
                            manager.next(requestHttp, responseHttp);
                            if (loader.getBooleanProperty("maintenance.enable", true)) {
                                try {
                                    String html = Files.readString(Paths.get("src/main/resources/maintenance.html"), StandardCharsets.UTF_8);
                                    log.debug("Arquivo HTML Carregado: "+"src/main/resources/maintenance.html" );
                                    responseHttp.withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                            .withHeader("Content-Type", "text/html; charset=UTF-8")
                                            .withBody(html);

                                } catch (IOException e) {
                                    String fallback = loader.getProperty("maintenance.message", "Servidor em manutenção.");
                                    responseHttp.withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                            .withHeader("Content-Type", "text/plain; charset=UTF-8")
                                            .withBody(fallback);
                                }
                                out.write(responseHttp.build().getBytes(StandardCharsets.UTF_8));
                                out.flush();
                                continue;
                            }
                            String staticPath = "src/main/resources" + requestHttp.getPath();
                            if (Files.exists(Paths.get(staticPath))
                                    &&
                                    !Files.isDirectory(Paths.get(staticPath))) {
                                String contentType = getContentType(requestHttp.getPath());
                                responseHttp.withStatus(HttpStatus.OK)
                                        .withHeader("Content-Type", contentType)
                                        .withBody(Files.readString(Paths.get(staticPath), StandardCharsets.UTF_8));
                                out.write(responseHttp.build().getBytes(StandardCharsets.UTF_8));
                                out.flush();
                                log.info("Arquivo estático servido: " + staticPath);
                                continue;
                            }

                            HttpStatus routeStatus = routes.get(requestHttp.getPath());
                            if (routeStatus != null) {
                                responseHttp.withStatus(routeStatus)
                                        .withHeader("Content-Type", "text/html; charset=UTF-8");

                                if (requestHttp.getPath().equals("/") || requestHttp.getPath().equals("/index")) {
                                    try {
                                        String html = Files.readString(Paths.get("src/main/resources/index.html"), StandardCharsets.UTF_8);
                                        responseHttp.withBody(html);
                                    } catch (IOException e) {
                                        responseHttp.withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .withBody("<h1>Erro ao carregar a página HTML</h1>");
                                    }
                                } else if (requestHttp.getPath().equals("/hello")) {
                                    String name = requestHttp.getQueryParams().getOrDefault("idade", "Visitante");
                                    responseHttp.withBody("<h1>Olá, " + name + "!</h1>");
                                } else {
                                    responseHttp.withBody("<p>Rota encontrada: " + requestHttp.getPath() + "</p>");
                                }
                            } else {
                                String html = Files.readString(Paths.get("src/main/resources/404.html"), StandardCharsets.UTF_8);
                                responseHttp.withStatus(HttpStatus.NOT_FOUND)
                                        .withHeader("Content-Type", "text/html; charset=UTF-8")
                                        .withBody(html);
                            }

                            out.write(responseHttp.build().getBytes(StandardCharsets.UTF_8));
                            out.flush();
                            log.info("Resposta enviada para " + clientIp);

                        } catch (IOException e) {
                            log.warn("Requisição inválida de " + clientIp + " - " + e.getMessage());

                            String badRequest = new ResponseHttp()
                                    .withStatus(HttpStatus.BAD_REQUEST)
                                    .withHeader("Content-Type", "text/plain; charset=UTF-8")
                                    .withBody("Requisição inválida.")
                                    .build();

                            out.write(badRequest.getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }

                    } catch (IOException e) {
                        log.error("Erro na conexão com cliente.", e);
                    }
                }

            } catch (IOException e) {
                log.error("Erro no servidor.", e);
            }
        }

        private static String getContentType(String path) {
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "application/octet-stream";
        }
    }
