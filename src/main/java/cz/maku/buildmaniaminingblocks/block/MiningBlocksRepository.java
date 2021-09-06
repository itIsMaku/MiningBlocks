package cz.maku.buildmaniaminingblocks.block;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import cz.maku.buildmaniaminingblocks.App;
import cz.maku.buildmaniaminingblocks.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class MiningBlocksRepository extends ArrayList<MiningBlock> {

    public void download() {
        App app = App.getInstance();
        for (String block : app.getConfig().getConfigurationSection("blocks").getKeys(false)) {
            ConfigurationSection blockSection = app.getConfig().getConfigurationSection("blocks").getConfigurationSection(block);
            World world = Bukkit.getWorld(blockSection.getString("location.world"));
            int x = blockSection.getInt("location.x");
            int y = blockSection.getInt("location.y");
            int z = blockSection.getInt("location.z");
            if (world == null) {
                throw new IllegalArgumentException("World does not exist.");
            }
            Block bukkitBlock = world.getBlockAt(x, y, z);

            World hologramWorld = Bukkit.getWorld(blockSection.getString("hologram-location.world"));
            double hologramX = blockSection.getDouble("hologram-location.x");
            double hologramY = blockSection.getDouble("hologram-location.y");
            double hologramZ = blockSection.getDouble("hologram-location.z");

            int storage = blockSection.getInt("storage");
            if (storage >= blockSection.getInt("needed")) {
                storage = 0;
                blockSection.set("storage", 0);
                app.saveConfig();
            }

            Hologram hologram = HologramsAPI.createHologram(app, new Location(hologramWorld, hologramX, hologramY, hologramZ));
            List<String> lines = new ArrayList<>();
            for (String configLine : blockSection.getStringList("hologram")) {
                String line = ChatColor.translateAlternateColorCodes('&',
                        Utils.translateHexColorCodes("#", "",
                        configLine
                        .replace("{mined}", String.valueOf(storage))
                        .replace("{max}", String.valueOf(blockSection.getInt("needed")))
                        .replace("{last_winner}", blockSection.getString("last-winner"))
                        .replace("{state}", app.getConfig().getConfigurationSection("state").getString("can")))
                );
                hologram.appendTextLine(line);
                lines.add(line);
            }
            MiningBlock miningBlock = new MiningBlock(
                    block, Material.getMaterial(blockSection.getString("material")), ChatColor.translateAlternateColorCodes('&', Utils.translateHexColorCodes("#", "", blockSection.getString("name"))),
                    hologram,
                    bukkitBlock,
                    blockSection.getInt("needed"),
                    storage,
                    blockSection.getString("last-winner"),
                    blockSection.getInt("progress-line"), blockSection.getInt("winner-line"), blockSection.getInt("state-line"), true);
            add(miningBlock);
        }
    }

}
