package utils;

import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonReader {

    private static final Map<String, JSONObject> CACHE = new ConcurrentHashMap<>();

    private static JSONObject load(String fileName) {
        try {
            String filePath = System.getProperty("user.dir")
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "data"
                    + File.separator + fileName;

            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return new JSONObject(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON file: " + fileName, e);
        }
    }

    public static String getTestData(String fileName, String section, String key) {
        JSONObject json = CACHE.computeIfAbsent(fileName, JsonReader::load);
        return json.getJSONObject(section).getString(key);
    }
}