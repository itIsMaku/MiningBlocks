package cz.maku.buildmaniaminingblocks.listener;

import cz.maku.buildmaniaminingblocks.App;
import cz.maku.buildmaniaminingblocks.Utils;
import cz.maku.buildmaniaminingblocks.block.MiningBlock;
import cz.maku.buildmaniaminingblocks.countdown.CountdownAPI;
import cz.maku.buildmaniaminingblocks.player.MiningPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        App.getInstance().getMiningPlayersRepository().register(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        App.getInstance().getMiningPlayersRepository().unregister(e.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        MiningBlock miningBlock = App.getInstance().getMiningBlocksRepository().stream().filter(mb -> mb.getBlock().equals(block)).findFirst().orElse(null);
        if (miningBlock == null) return;
        e.setCancelled(true);
        List<String> hologramLines = App.getInstance().getConfig()
                .getConfigurationSection("blocks")
                .getConfigurationSection(miningBlock.getKey())
                .getStringList("hologram");
        if (miningBlock.getMined() >= miningBlock.getNeeded() -1) {
            String line = ChatColor.translateAlternateColorCodes('&', hologramLines
                    .get(miningBlock.getProgressLine())
                    .replace("{mined}", String.valueOf(miningBlock.getNeeded()))
                    .replace("{max}", String.valueOf(miningBlock.getNeeded()))
            );
            miningBlock.getHologram().insertTextLine(miningBlock.getProgressLine(), line);
            miningBlock.getHologram().removeLine(miningBlock.getProgressLine() + 1);
            Map<MiningPlayer, Integer> minedBlocks = new HashMap<>();

            for (MiningPlayer contributor : miningBlock.getContributors()) {
                miningBlock.getBlock().setType(Material.BEDROCK);
                minedBlocks.put(contributor, contributor.getMined(miningBlock));
            }

            Map<MiningPlayer, Integer> sortedByBlocks = Utils.sortByValue(minedBlocks);
            for (Map.Entry<MiningPlayer, Integer> entry : sortedByBlocks.entrySet()) {
                String winner = entry.getKey().getBukkit().getName();
                String hologramLine = ChatColor.translateAlternateColorCodes('&', hologramLines
                        .get(miningBlock.getWinnerLine())
                        .replace("{last_winner}", winner)
                );
                miningBlock.getHologram().insertTextLine(miningBlock.getWinnerLine(), hologramLine);
                miningBlock.getHologram().removeLine(miningBlock.getWinnerLine() + 1);
                break;
            }

            for (Map.Entry<MiningPlayer, Integer> entry : minedBlocks.entrySet()) {
                MiningPlayer miningPlayer = entry.getKey();
                int mined = entry.getValue();
                ConfigurationSection section = App.getInstance().getConfig().getConfigurationSection("blocks").getConfigurationSection(miningBlock.getKey()).getConfigurationSection("rewards");
                ConfigurationSection finalRewardSection = null;
                for (String key : section.getKeys(false)) {
                    ConfigurationSection rewardSection = section.getConfigurationSection(key);
                    int required = rewardSection.getInt("required");
                    if (required < mined || required == mined) {
                        finalRewardSection = rewardSection;
                    } else {
                        break;
                    }
                }
                if (finalRewardSection != null) {
                    if (miningPlayer == null || !miningPlayer.getBukkit().isOnline()) continue;
                    for (String command : finalRewardSection.getStringList("commands")) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", miningPlayer.getBukkit().getName()));
                    }
                    for (String message : finalRewardSection.getStringList("messages")) {
                        miningPlayer.getBukkit().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }

            miningBlock.getContributors().clear();

            CountdownAPI countdown = new CountdownAPI(App.getInstance(), App.getInstance().getConfig().getInt("reset-after"));
            countdown.setHandler(new CountdownAPI.CountdownHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {
                    miningBlock.setMined(0);
                    App.getInstance().getConfig()
                            .getConfigurationSection("blocks")
                            .getConfigurationSection(miningBlock.getKey())
                            .set("storage", 0);
                    App.getInstance().saveConfig();
                    miningBlock.setActivated(true);
                    miningBlock.getBlock().setType(miningBlock.getMaterial());

                    String stateHologramLine = ChatColor.translateAlternateColorCodes('&', hologramLines
                            .get(miningBlock.getStateLine())
                            .replace("{state}", App.getInstance().getConfig().getConfigurationSection("state").getString("can"))
                    );
                    miningBlock.getHologram().insertTextLine(miningBlock.getStateLine(), stateHologramLine);
                    miningBlock.getHologram().removeLine(miningBlock.getStateLine() + 1);

                    String hologramLine = ChatColor.translateAlternateColorCodes('&', hologramLines
                            .get(miningBlock.getProgressLine())
                            .replace("{mined}", String.valueOf(miningBlock.getMined()))
                            .replace("{max}", String.valueOf(miningBlock.getNeeded()))
                    );
                    miningBlock.getHologram().insertTextLine(miningBlock.getProgressLine(), hologramLine);
                    miningBlock.getHologram().removeLine(miningBlock.getProgressLine() + 1);
                }

                @Override
                public void onTick() {
                    String seconds = "";
                    switch (countdown.getTime()) {
                        case 1:
                            seconds = " vteřinu";
                            break;
                        case 2:
                        case 3:
                        case 4:
                            seconds = " vteřiny";
                            break;
                        default:
                            seconds = " vteřin";
                    }
                    String stateHologramLine = ChatColor.translateAlternateColorCodes('&', hologramLines
                            .get(miningBlock.getStateLine())
                            .replace("{state}", App.getInstance().getConfig().getConfigurationSection("state").getString("cannot").replace("{cas}", String.valueOf(countdown.getTime()) + seconds))
                    );
                    miningBlock.getHologram().insertTextLine(miningBlock.getStateLine(), stateHologramLine);
                    miningBlock.getHologram().removeLine(miningBlock.getStateLine() + 1);
                }
            });
            countdown.start();
            miningBlock.setActivated(false);
            return;
        }
        MiningPlayer miningPlayer = App.getInstance().getMiningPlayersRepository().get(e.getPlayer());
        miningPlayer.addMined(miningBlock, 1);
        miningBlock.addMined(1);
        if (!miningBlock.getContributors().contains(miningPlayer)) {
            miningBlock.getContributors().add(miningPlayer);
        }
        String hologramLine = ChatColor.translateAlternateColorCodes('&', hologramLines
                .get(miningBlock.getProgressLine())
                .replace("{mined}", String.valueOf(miningBlock.getMined()))
                .replace("{max}", String.valueOf(miningBlock.getNeeded()))
        );
        miningBlock.getHologram().insertTextLine(miningBlock.getProgressLine(), hologramLine);
        miningBlock.getHologram().removeLine(miningBlock.getProgressLine() + 1);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getItemMeta() == null) return;
        if (e.getItemInHand().getItemMeta().getDisplayName().startsWith("§eSETUP §8- ")) {
            String displayName = e.getItemInHand().getItemMeta().getDisplayName().replace("§eSETUP §8- ", "");
            MiningPlayer miningPlayer = App.getInstance().getMiningPlayersRepository().get(e.getPlayer());
            MiningBlock miningBlock = App.getInstance().getMiningBlocksRepository().stream().filter(mb -> mb.getDisplayName().equals(displayName)).findFirst().orElse(null);
            if (miningBlock != null) {
                String worldName = e.getPlayer().getWorld().getName();
                int x = e.getBlock().getX();
                int y = e.getBlock().getY();
                int z = e.getBlock().getZ();
                FileConfiguration config = App.getInstance().getConfig();
                ConfigurationSection blockSection = config.getConfigurationSection("blocks." + miningBlock.getKey());
                blockSection.set("location.world", worldName);
                blockSection.set("location.x", x);
                blockSection.set("location.y", y);
                blockSection.set("location.z", z);
                App.getInstance().saveConfig();

                miningBlock.setBlock(e.getBlock());

                World hologramWorld = Bukkit.getWorld(blockSection.getString("hologram-location.world"));
                double hologramX = blockSection.getDouble("hologram-location.x");
                double hologramY = blockSection.getDouble("hologram-location.y");
                double hologramZ = blockSection.getDouble("hologram-location.z");


                miningPlayer.getBukkit().sendMessage("§aBlock registered.");
                miningBlock.getHologram().teleport(new Location(hologramWorld, hologramX, hologramY, hologramZ));
                miningPlayer.getBukkit().sendMessage("§aHologram updated.");
            }
        }


    }

}
