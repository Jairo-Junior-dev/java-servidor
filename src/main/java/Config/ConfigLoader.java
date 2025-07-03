package Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static ConfigLoader instance;
    private final Properties properties = new Properties();
    private final String fileConfig;

    private Looger looger = Looger.getInstance();

    private ConfigLoader(String fileConfig) {
        this.fileConfig = fileConfig;
        loadProperties();
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader("src/main/resources/config.service.properties");
        }
        return instance;
    }

    public static ConfigLoader getInstance(String fileConfig) {
        if (instance == null) {
            instance = new ConfigLoader(fileConfig);
        }
        return instance;
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream(fileConfig)) {
            properties.load(fis);
            looger.debug("[ConfigLoader] Arquivo de configuração carregado com sucesso.");
        } catch (IOException e) {
            looger.error("Erro ao carregar o arquivo de configuração: " , e);
            throw new RuntimeException("Erro ao carregar o arquivo de configuração: " + fileConfig, e);

        }
    }


    public String getProperty(String key) {
        looger.debug("Chave: "+key);
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (Exception e) {
            looger.warn("Porta não encontrada:  Retornando Porta Padrão "+ defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(properties.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Properties getAllProperties() {
        return properties;
    }

}
