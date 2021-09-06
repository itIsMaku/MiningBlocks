package cz.maku.buildmaniaminingblocks.command;

import cz.maku.buildmaniaminingblocks.App;
import cz.maku.buildmaniaminingblocks.block.MiningBlock;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Setup implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("buildmania.miningblock.admin")) {
            player.sendMessage("§cYou are not allowed to do this!");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(" §e/setup hologram <id> §8- §7Set hologram location to your location");
            player.sendMessage(" §e/setup block <id> §8- §7Gives you setup block.");
            return true;
        }
        String id = args[1];
        MiningBlock miningBlock = App.getInstance().getMiningBlocksRepository().stream().filter(mb -> mb.getKey().equals(id)).findFirst().orElse(null);
        if (miningBlock == null) {
            player.sendMessage("§cBlock with this ID does not exist.");
            return true;
        }
        switch (args[0]) {
            case "block":
                ItemStack itemStack = new ItemStack(miningBlock.getMaterial(), 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§eSETUP §8- " + miningBlock.getDisplayName());
                itemStack.setItemMeta(itemMeta);
                player.getInventory().addItem(itemStack);
                player.sendMessage("§ePlace block for setup mining block.");
                break;
            case "hologram":
                ConfigurationSection blockSection = App.getInstance().getConfig().getConfigurationSection("blocks." + miningBlock.getKey());
                Location location = player.getLocation();
                blockSection.set("hologram-location.world", location.getWorld().getName());
                blockSection.set("hologram-location.x", location.getX());
                blockSection.set("hologram-location.y", location.getY());
                blockSection.set("hologram-location.z", location.getZ());
                miningBlock.getHologram().teleport(location.getWorld(), location.getX(), location.getY(), location.getZ());
                App.getInstance().saveConfig();
                player.sendMessage("§aHologram location updated.");
                break;
        }
        return false;
    }
}
