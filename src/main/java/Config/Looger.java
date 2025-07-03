package Config;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Looger {
    private static Looger instance;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter fileDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String currentLevel = "DEBUG";
    private final String logDirectory = "./logs";

    private Looger() {
        File dir = new File(logDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static Looger getInstance() {
        if (instance == null) {
            instance = new Looger();
        }
        return instance;
    }

    public void setLevel(String level) {
        currentLevel = level.toUpperCase();
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void error(String message, Exception e) {
        log("ERROR", message + " - " + e.getMessage());
        e.printStackTrace();
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    private void log(String level, String message) {
        if (!shouldLog(level)) {
            return;
        }

        String timestamp = dtf.format(LocalDateTime.now());
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        String color = getColor(level);
        String reset = "\u001B[0m";
        System.out.println(color + formattedMessage + reset);

        saveToFile(formattedMessage);
    }

    private boolean shouldLog(String level) {
        List<String> levels = List.of("DEBUG", "INFO", "WARN", "ERROR");
        return levels.indexOf(level) >= levels.indexOf(currentLevel);
    }

    private void saveToFile(String message) {
        String filename = logDirectory + "/log-" + fileDtf.format(LocalDateTime.now()) + ".log";
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(message + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Erro ao gravar log no arquivo: " + e.getMessage());
        }
    }

    private String getColor(String level) {
        return switch (level) {
            case "INFO" -> "\u001B[32m";   // Verde
            case "WARN" -> "\u001B[33m";   // Amarelo
            case "ERROR" -> "\u001B[31m";  // Vermelho
            case "DEBUG" -> "\u001B[34m";  // Azul
            default -> "\u001B[0m";        // Reset
        };
    }
}
