package utils;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonReader {

    public static String getTestData(String fileName, String userType, String key) {
        try {
            String filePath = System.getProperty("user.dir")
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "data"
                    + File.separator + fileName;

            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);

            return jsonObject.getJSONObject(userType).getString(key);

        } catch (Exception e) {
            System.out.println("Failed to read JSON file!");
            e.printStackTrace();
            return null;
        }
    }
}