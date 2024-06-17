package transfem.order.gitcraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    private static final String CONFIG_PATH = "src/main/resources/config.json";
    private static String WORLD_NAME;
    private static int CHUNK_RADIUS;  
    
    public Config() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return;
        }
        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JsonElement element = jsonObject.get("worldName");
        if (element != null) {
            WORLD_NAME = element.getAsString();
        }
        element = jsonObject.get("chunkSize");
        if (element != null) {
            CHUNK_RADIUS = element.getAsInt();
        }
    }

    public static String WorldName() {
    return WORLD_NAME;
    }

    public static int ChunkRadius() {
        return CHUNK_RADIUS;
    }

    public static boolean setWorldName(String worldName) {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return false;
        }
        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JsonElement element = jsonObject.get("worldName");
        if (element == null) {
            return false;
        }
        jsonObject.addProperty("worldName", worldName);
        WORLD_NAME = worldName;
        return true;
    }

    public static boolean setChunkRadius(int chunkSize) {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return false;
        }
        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JsonElement element = jsonObject.get("chunkSize");
        if (element == null) {
            return false;
        }
        jsonObject.addProperty("chunkSize", chunkSize);
        CHUNK_RADIUS = chunkSize;
        return true;
    }
}
