package net.runelite.client.plugins.glove;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;

public enum GloveID
{
    INVALID(-1),
    MAGIC_PROJECTILE(1707),
    MAGIC_PRAYER_PROJECTILE(1713),
    RANGED_PROJECTILE(1711),
    TORNADO_NPC(9025),
    FIRE_PROJECTILE_ANIMATION(8419),
    SPAWN_TORNADO_ANIMATION(8418),
    MELEE_PROTECT_NPC_ID(9021),
    RANGED_PROTECT_NPC_ID(9022),
    MAGIC_PROTECT_NPC_ID(9023),

    CORRUPTED_MAGIC_PROJECTILE(1708),
    CORRUPTED_MAGIC_PRAYER_PROJECTILE(1714),
    CORRUPTED_RANGED_PROJECTILE(1712),
    CORRUPTED_TORNADO_NPC(9039),
    CORRUPTED_MELEE_PROTECT_NPC_ID(9035),
    CORRUPTED_RANGED_PROTECT_NPC_ID(9036),
    CORRUPTED_MAGIC_PROTECT_NPC_ID(9037);



    private final int id;
    private static final Map<Integer, GloveID> idMap = Maps.newHashMap();

    static {
        for(GloveID id : GloveID.values())
        {
            idMap.put(id.getId(), id);
        }
    }

    public final int getId()
    {
        return id;
    }

    public static GloveID getFromValue(int id)
    {
        return idMap.getOrDefault(id, INVALID);
    }

    GloveID(int id)
    {
        this.id = id;
    }
}
