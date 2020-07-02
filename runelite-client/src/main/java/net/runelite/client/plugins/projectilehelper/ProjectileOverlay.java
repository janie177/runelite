package net.runelite.client.plugins.projectilehelper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.cannon.CannonConfig;
import net.runelite.client.plugins.cannon.CannonPlugin;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class ProjectileOverlay extends Overlay {

    private final Client client;
    private final ProjectileHelper plugin;
    private final ProjectileHelperConfig config;
    private static final Font font_hit = FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD, 18);
    private static final Font font_normal = FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD, 16);
    private static final Color color_normal = Color.WHITE;
    private static final Color color_hit = Color.RED;

    @Inject
    ProjectileOverlay(Client client, ProjectileHelper plugin, ProjectileHelperConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        plugin.projectiles.entrySet().forEach(e ->{
            Projectile p = e.getKey();
            long ping = e.getValue();

            //How many ticks this projectile will be alive for.
            int remainingTicks = (int) ((((p.getRemainingCycles() * 20L) - ping) / 600L));


            //Color to use.
            Color color;

            if(remainingTicks  == 0)
            {
                graphics.setFont(font_hit);
                color = color_hit;
            }
            else
            {
                graphics.setFont(font_normal);
                color = color_normal;
            }

            String text = Integer.toString(remainingTicks);
            int x = (int) p.getX();
            int y = (int) p.getY();
            LocalPoint projectilePoint = new LocalPoint(x, y);
            Point textLocation = Perspective.getCanvasTextLocation(client, graphics, projectilePoint, text, 1);
            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
            }
        });

        return null;
    }
}
