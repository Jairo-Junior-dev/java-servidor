import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Route {

    ROOT("/"),
    INDEX("/index.html");

    private final String path;

    private static final Set<String> paths =
            Stream.of(values()).map(Route::getPath).collect(Collectors.toSet());

    Route(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static boolean isValid(String path) {
        return paths.contains(path);
    }
}
