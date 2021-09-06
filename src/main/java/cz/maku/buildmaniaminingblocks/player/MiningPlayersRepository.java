package cz.maku.buildmaniaminingblocks.player;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class MiningPlayersRepository extends HashMap<Player, MiningPlayer> {

    public void register(Player player) {
        put(player, new MiningPlayer(player));
    }

    public void unregister(Player player) {
        remove(player);
    }

}
