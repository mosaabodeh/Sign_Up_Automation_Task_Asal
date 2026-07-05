package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    public static synchronized void loadConfig(String fileName) {
        if (loaded) return;

        try (FileInputStream input =
                     new FileInputStream("src/test/resources/" + fileName)) {

            properties.load(input);
            loaded = true;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + fileName, e);
        }
    }

    public static String getProperty(String key) {
        if (!loaded) {
            loadConfig("emulator.properties");
        }
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (!loaded) {
            loadConfig("emulator.properties");
        }
        return properties.getProperty(key, defaultValue);
    }
}