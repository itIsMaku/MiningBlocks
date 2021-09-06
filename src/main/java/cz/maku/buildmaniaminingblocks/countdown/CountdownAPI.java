package cz.maku.buildmaniaminingblocks.countdown;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class CountdownAPI {

    private final JavaPlugin plugin;
    private int time;
    private BukkitTask task;
    private CountdownHandler handler;
    private boolean finished;

    public CountdownAPI(JavaPlugin plugin, int length) {
        this.plugin = plugin;
        this.time = length;
    }

    public void start() {
        handler.onStart();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (time < 1) {
                    finished = true;
                    handler.onFinish();
                    task.cancel();
                    return;
                }
                handler.onTick();
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        task.cancel();
    }

    public boolean isFinished() {
        return finished;
    }

    public void setHandler(CountdownHandler handler) {
        this.handler = handler;
    }

    public int getTime() {
        return time;
    }

    public interface CountdownHandler {
        void onStart();

        void onFinish();

        void onTick();
    }

}