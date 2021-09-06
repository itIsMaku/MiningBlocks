package cz.maku.buildmaniaminingblocks.command;

import cz.maku.buildmaniaminingblocks.App;
import cz.maku.buildmaniaminingblocks.block.MiningBlock;
import cz.maku.buildmaniaminingblocks.block.MiningBlocksRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("buildmania.miningblock.admin")) {
            player.sendMessage("§cYou are not allowed to do this!");
            return true;
        }
        App.getInstance().reloadConfig();
        MiningBlocksRepository miningBlocksRepository = App.getInstance().getMiningBlocksRepository();
        App.getInstance().saveBlocksStorage();
        for (MiningBlock miningBlock : miningBlocksRepository) {
            miningBlock.getHologram().delete();
        }
        miningBlocksRepository.clear();
        miningBlocksRepository.download();
        player.sendMessage("§aMiningBlocks reloaded.");
        return false;
    }
}
