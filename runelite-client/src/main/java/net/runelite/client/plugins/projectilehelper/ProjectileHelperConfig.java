package net.runelite.client.plugins.projectilehelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("projectilehelper")
public interface ProjectileHelperConfig extends Config {
    @ConfigItem(
            keyName = "pingInterval",
            name = "Ping Interval",
            description = "How often to update the ping in game ticks."
    )
    default int pingInterval()
    {
        return 10;
    }
}