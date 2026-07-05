package utils;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReader {

    private static JSONObject cachedJson;

    private static void load(String fileName) {
        try {
            String filePath = System.getProperty("user.dir")
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "data"
                    + File.separator + fileName;

            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            cachedJson = new JSONObject(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON file", e);
        }
    }

    public static String getTestData(String fileName, String section, String key) {
        if (cachedJson == null) {
            load(fileName);
        }

        return cachedJson.getJSONObject(section).getString(key);
    }
}