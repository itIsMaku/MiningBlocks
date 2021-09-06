package cz.maku.buildmaniaminingblocks.player;

import com.google.common.collect.Maps;
import cz.maku.buildmaniaminingblocks.App;
import cz.maku.buildmaniaminingblocks.block.MiningBlock;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;

@Getter
public class MiningPlayer {

    private final Player bukkit;
    private final Map<MiningBlock, Integer> minedStorage;

    public MiningPlayer(Player bukkit) {
        this.bukkit = bukkit;
        this.minedStorage = Maps.newHashMap();
        for (MiningBlock miningBlock : App.getInstance().getMiningBlocksRepository()) {
            minedStorage.put(miningBlock, 0);
        }
    }

    public int getMined(MiningBlock miningBlock) {
        return minedStorage.get(miningBlock);
    }

    public void setMined(MiningBlock miningBlock, int mined) {
       minedStorage.put(miningBlock, mined);
    }

    public void addMined(MiningBlock miningBlock, int mined) {
        setMined(miningBlock, getMined(miningBlock) + mined);
    }
}
