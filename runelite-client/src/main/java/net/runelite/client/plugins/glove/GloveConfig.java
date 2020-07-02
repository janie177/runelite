package net.runelite.client.plugins.glove;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("glove")
public interface GloveConfig extends Config {
    @ConfigItem(
            keyName = "prayerNotify",
            name = "Notify Prayer Disable",
            description = "When your prayers are disabled, this will notify you."
    )
    default boolean notifyOnPrayerDisable()
    {
        return true;
    }

    @ConfigItem(
            keyName = "prayerNotifyDuration",
            name = "Prayer notification duration",
            description = "How long to show the prayer warning for?"
    )
    default int getPrayerNotificationDuration()
    {
        return 2200;
    }
}
