package net.runelite.client.plugins.projectilehelper;

import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Projectile;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.worldhopper.ping.Ping;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ExecutorServiceExceptionLogger;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(
        name = "Projectile Helper",
        description = "Displays information about projectiles.",
        tags = {"Projectile", "Timer", "Debug", "Info", "Lifetime", "Arrow", "Magic", "Ranged", "Impact"}
)
public class ProjectileHelper extends Plugin {

    //Registry of projectiles and their spawn ticks.
    public Map<Projectile, Long> projectiles = Maps.newConcurrentMap();

    //Current ping.
    @Getter
    private long ping = 0;

    //Timestamp of the last game tick.
    private long lastTick = 0;

    //Scheduled ping task.
    private ScheduledFuture<?> pingFuture;

    //Last ping update in ticks.
    int lastPingUpdate = 0;

    //Executor used to schedule tasks.
    private ScheduledExecutorService executor;

    @Inject
    private Client client;

    @Inject
    private Provider<MenuManager> menuManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private WorldService worldService;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ProjectileOverlay overlay;

    @Inject
    private ProjectileHelperConfig config;

    @Provides
    ProjectileHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ProjectileHelperConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        executor = new ExecutorServiceExceptionLogger(Executors.newSingleThreadScheduledExecutor());
        pingFuture = executor.scheduleWithFixedDelay(this::UpdatePing, 15, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        projectiles.clear();

        if(pingFuture != null)
        {
            pingFuture.cancel(true);
            pingFuture = null;
        }
        executor.shutdown();
        executor = null;
    }



    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        Projectile projectile = event.getProjectile();
        if(projectile != null && !projectiles.containsKey(projectile))
        {
            projectiles.put(projectile, ping);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        ++lastPingUpdate;
        if(lastPingUpdate >= config.pingInterval() && pingFuture.isDone())
        {
            pingFuture = executor.scheduleWithFixedDelay(this::UpdatePing, 15, 1, TimeUnit.SECONDS);
            lastPingUpdate = 0;
        }

        //Remove old projectiles.
        projectiles.entrySet().removeIf(p -> p.getKey().getRemainingCycles() <= 0);
        lastTick = System.currentTimeMillis();
    }

    public long getTimeTillTick()
    {
        return Math.max(600L - (System.currentTimeMillis() - lastTick), 0L);
    }

    private void UpdatePing()
    {
        //Update ping.
        WorldResult worldResult = worldService.getWorlds();
        // There is no reason to ping the current world if not logged in, as the overlay doesn't draw
        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        final World currentWorld = worldResult.findWorld(client.getWorld());
        if (currentWorld != null)
        {
            ping = Ping.ping(currentWorld);
            return;
        }
    }
}
