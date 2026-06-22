package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    //  emulator.properties or realdevice.properties)
    public static void loadConfig(String fileName) {
        try {
            properties = new Properties();

            FileInputStream input = new FileInputStream("src/test/resources/" + fileName);
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load properties file: " + fileName);
        }
    }

    public static String getProperty(String key) {
        if (properties == null) {
            //Default Emulator
            loadConfig("emulator.properties");
        }
        return properties.getProperty(key);
    }
}
