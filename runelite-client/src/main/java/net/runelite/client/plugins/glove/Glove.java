package net.runelite.client.plugins.glove;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Glove Enhancements",
        description = "Makes wearing gloves more comfortable.",
        tags = {"custom", "glove"}
)
public class Glove extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Provider<MenuManager> menuManager;

    @Inject
    private GloveConfig config;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ItemManager itemManager;

    @Inject
    private GloveFight fight;

    @Inject
    private GloveOverlay overlay;

    @Provides
    GloveConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GloveConfig.class);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {

    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        Actor actor = event.getActor();
        if(actor instanceof NPC)
        {
            NPC npc = (NPC) actor;
            int id = npc.getId();

            if(isTheBoss(id))
            {
                //Attack Animation Thingy (projectile or tornado).
                int animation = npc.getAnimation();
                if(animation == GloveID.FIRE_PROJECTILE_ANIMATION.getId() || animation == GloveID.SPAWN_TORNADO_ANIMATION.getId())
                {
                    fight.onNPCAttack(animation);
                    overlay.update(fight);
                }
            }
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        int id = event.getNpc().getId();

        //NPC ID detected (melee, ranged, magic).
        if(isTheBoss(id))
        {
            overlay.setEnabled(false);
            fight.reset();
            overlay.update(fight);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        int id = event.getNpc().getId();

        //NPC ID detected (melee, ranged, magic).
        if(isTheBoss(id))
        {
            fight.reset();
            fight.updateBoss((NPC)event.getActor());
            overlay.setEnabled(true);
            overlay.update(fight);
        }
    }

    /**
     *	Listen for projectiles
     */
    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        if(event.getProjectile() != null)
        {
            int projectileID = event.getProjectile().getId();
            fight.onProjectile(projectileID);
            overlay.update(fight);
        }
    }

    private boolean isTheBoss(int id)
    {
        return id == GloveID.MELEE_PROTECT_NPC_ID.getId() || id == GloveID.MAGIC_PROTECT_NPC_ID.getId() || id == GloveID.RANGED_PROTECT_NPC_ID.getId()
                || id == GloveID.CORRUPTED_MAGIC_PROTECT_NPC_ID.getId() || id == GloveID.CORRUPTED_RANGED_PROTECT_NPC_ID.getId() || id == GloveID.CORRUPTED_MELEE_PROTECT_NPC_ID.getId();
    }
}
