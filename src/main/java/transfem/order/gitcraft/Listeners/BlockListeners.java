package transfem.order.gitcraft.Listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.bukkit.Chunk;
import java.io.FileWriter;
import java.io.IOException;



import java.io.FileNotFoundException;
import java.io.FileReader;

public class BlockListeners implements Listener {

        private void logChunk(BlockEvent event) {
            if (event.getBlock().getWorld().getName().startsWith("git") && new File(event.getBlock().getWorld().getName() + ".json").exists()) {
                // Check if the chunk the block is in already in the chunks array in the json file
                try {
                    FileReader reader = new FileReader(event.getBlock().getWorld().getName() + ".json");
                    Gson gson = new Gson();
                    // Check if the chunk id is in the json file
                    // if it is, do nothing
                    // else add the chunk id to the json file
                    Chunk chunk = event.getBlock().getChunk();
                    String chunkId = chunk.getX() + "_" + chunk.getZ();
                    JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonArray chunksArray = jsonObject.getAsJsonArray("chunks");
                    if (!chunksArray.contains(gson.toJsonTree(chunkId))) {
                        // Chunk id is not in the json file, add it
                        chunksArray.add(chunkId);
                        // Save the updated json file
                        try {
                            FileWriter writer = new FileWriter(event.getBlock().getWorld().getName() + ".json");
                            gson.toJson(jsonObject, writer);
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }




            }
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event){
            logChunk(event);
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            logChunk(event);
        }
}