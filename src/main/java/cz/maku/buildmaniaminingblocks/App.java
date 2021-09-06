package cz.maku.buildmaniaminingblocks;

import cz.maku.buildmaniaminingblocks.block.MiningBlock;
import cz.maku.buildmaniaminingblocks.block.MiningBlocksRepository;
import cz.maku.buildmaniaminingblocks.command.Reload;
import cz.maku.buildmaniaminingblocks.command.Setup;
import cz.maku.buildmaniaminingblocks.listener.BukkitListener;
import cz.maku.buildmaniaminingblocks.player.MiningPlayersRepository;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class App extends JavaPlugin {

    @Getter
    private static App instance;
    private MiningPlayersRepository miningPlayersRepository;
    private MiningBlocksRepository miningBlocksRepository;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        instance = this;
        miningPlayersRepository = new MiningPlayersRepository();
        miningBlocksRepository = new MiningBlocksRepository();
        miningBlocksRepository.download();

        getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        getCommand("setup").setExecutor(new Setup());
        getCommand("reload").setExecutor(new Reload());

        Bukkit.getScheduler().runTaskTimer(this, this::saveBlocksStorage, 0, 20 * 300);
    }

    @Override
    public void onDisable() {
        saveBlocksStorage();
    }

    public void saveBlocksStorage() {
        for (MiningBlock miningBlock : miningBlocksRepository) {
            getConfig().getConfigurationSection("blocks").getConfigurationSection(miningBlock.getKey()).set("storage", miningBlock.getMined());
            saveConfig();
        }
    }
}
