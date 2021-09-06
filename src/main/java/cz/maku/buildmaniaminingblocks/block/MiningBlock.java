package cz.maku.buildmaniaminingblocks.block;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.collect.Lists;
import cz.maku.buildmaniaminingblocks.player.MiningPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class MiningBlock {

    private final String key;
    private final Material material;
    private final String displayName;
    private final Hologram hologram;
    private final List<MiningPlayer> contributors;
    @Setter
    private Block block;
    private final int needed;
    @Setter
    @Nullable
    private String lastWinner;
    @Setter
    private int mined;
    private final int progressLine;
    private final int winnerLine;
    private final int stateLine;
    @Setter
    private boolean activated;

    public MiningBlock(String key, Material material, String displayName, Hologram hologram, Block block, int needed, int mined, @Nullable String lastWinner, int progressLine, int winnerLine, int stateLine, boolean activated) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.hologram = hologram;
        this.needed = needed;
        this.progressLine = progressLine;
        this.winnerLine = winnerLine;
        this.stateLine = stateLine;
        this.activated = activated;
        this.contributors = Lists.newArrayList();
        this.block = block;
        this.mined = mined;
        this.lastWinner = lastWinner;
    }

    public void addMined(int mined) {
        setMined(getMined() + mined);
    }
}
