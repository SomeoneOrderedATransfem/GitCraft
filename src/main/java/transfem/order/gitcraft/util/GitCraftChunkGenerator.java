package transfem.order.gitcraft.util;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;


import java.util.Random;


public class GitCraftChunkGenerator extends ChunkGenerator {
    @SuppressWarnings("deprecation")
    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);
    }
}
