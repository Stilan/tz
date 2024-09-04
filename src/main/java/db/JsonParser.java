package db;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;

public class JsonParser {
    private static final Gson gson = new Gson();

    public static JsonObject parseJson(String filePath) throws IOException {
        return gson.fromJson(new FileReader(filePath), JsonObject.class);
    }
}
