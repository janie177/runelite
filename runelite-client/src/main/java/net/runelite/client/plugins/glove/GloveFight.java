package net.runelite.client.plugins.glove;

import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.SoundEffectVolume;

import javax.inject.Inject;

public class GloveFight {

    @Inject
    private Client client;

    @Getter
    private int npcAttackCount;

    @Getter
    private long lastPrayerWarning;

    @Getter
    GloveAttackStyle style;

    @Getter
    private NPC boss;

    GloveFight()
    {
        reset();
    }

    void updateBoss(NPC boss)
    {
        this.boss = boss;
    }

    void onNPCAttack(int animation)
    {
        ++npcAttackCount;

        if(npcAttackCount >= 4)
        {
            npcAttackCount = 0;

            //Switch to magic.
            if(style == GloveAttackStyle.RANGED)
            {
                style = GloveAttackStyle.MAGIC;

                //Ice barrage hit
                client.playSoundEffect(168, SoundEffectVolume.HIGH);
            }
            //Switch to ranged.
            else
            {
                style = GloveAttackStyle.RANGED;
                //Smithing
                client.playSoundEffect(3791, SoundEffectVolume.HIGH);
            }
        }
    }

    public void onProjectile(int id)
    {
        if(id == GloveID.MAGIC_PRAYER_PROJECTILE.getId() || id == GloveID.CORRUPTED_MAGIC_PRAYER_PROJECTILE.getId())
        {
            lastPrayerWarning = System.currentTimeMillis();
        }
    }

    public int getAttacksLeft()
    {
        return 4 - npcAttackCount;
    }

    public void reset()
    {
        lastPrayerWarning = 0;
        npcAttackCount = 0;
        style = GloveAttackStyle.RANGED;
        boss = null;
    }


}
