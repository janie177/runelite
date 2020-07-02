package net.runelite.client.plugins.glove;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.*;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class GloveOverlay extends Overlay {

    private ItemManager itemManager;

    @Inject
    private Client client;
    private SkillIconManager skillIconManager;

    private int PRAYER_WARNING_DIMENSION_X;
    private int PRAYER_WARNING_DIMENSION_Y;

    private float maxDistance = 1500.f;

    private Glove plugin;
    private GloveConfig config;
    private boolean enabled;
    private ProgressPieComponent pieComponent = new ProgressPieComponent();

    //Large overlay thing.
    private BufferedImage largeBow;
    private BufferedImage largeStaff;
    private ImageComponent staffComponent;
    private ImageComponent bowComponent;
    private PanelComponent panel;


    //Last time the prayer warning was fired.
    long prayerWarning = 0;

    //Cached data that is displayed
    GloveAttackStyle attackStyle = GloveAttackStyle.RANGED;
    private BufferedImage attackStyleImage = null;
    NPC boss = null;


    @Inject
    public GloveOverlay(Glove glove, GloveConfig config, ItemManager itemManager, SkillIconManager skillIconManager) {
        super(glove);

        this.plugin = glove;
        this.config = config;
        this.enabled = true;
        this.itemManager = itemManager;
        this.skillIconManager = skillIconManager;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);

        pieComponent.setDiameter(40);

        //Initiate the overlay components.
        BufferedImage s = skillIconManager.getSkillImage(Skill.MAGIC);//itemManager.getImage(11709, 1, false);
        BufferedImage b = skillIconManager.getSkillImage(Skill.RANGED);

        PRAYER_WARNING_DIMENSION_X = s.getWidth() * 15;
        PRAYER_WARNING_DIMENSION_Y = s.getHeight() * 15;

        largeBow = ImageUtil.resizeImage(b, PRAYER_WARNING_DIMENSION_X, PRAYER_WARNING_DIMENSION_Y);
        largeStaff = ImageUtil.resizeImage(s, PRAYER_WARNING_DIMENSION_X, PRAYER_WARNING_DIMENSION_Y);

        largeBow = ImageUtil.alphaOffset(largeBow, -128);
        largeStaff = ImageUtil.alphaOffset(largeStaff, -128);

        bowComponent = new ImageComponent(largeBow);
        staffComponent = new ImageComponent(largeStaff);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        //Only draw if enabled.
        if(!enabled || boss == null || client.getPlane() != boss.getWorldLocation().getPlane())
        {
            return null;
        }

        //Disable if boss is no longer valid
        if(boss.isDead())
        {
            boss = null;
            enabled = false;
            return null;
        }

        //Make sure the thing is within range.
        LocalPoint playerLoc = client.getLocalPlayer().getLocalLocation();
        LocalPoint overlayPosition = boss.getLocalLocation();

        if (playerLoc != null && overlayPosition != null && overlayPosition.distanceTo(playerLoc) <= maxDistance)
        {
            //Render the attack style image above the boss.
            //Point pieChartLoc = Perspective.getCanvasImageLocation(client, overlayPosition, attackStyleImage, 60);
            //Point attackIconLoc = Perspective.getCanvasImageLocation(client, overlayPosition, attackStyleImage, 60);

            //Render the pie chart above the boss.
            pieComponent.setPosition(Perspective.localToCanvas(client, boss.getLocalLocation(), client.getPlane(),300));
            Dimension d =  pieComponent.render(graphics);

            //Render the attack style image above the boss.
            OverlayUtil.renderActorOverlayImage(graphics, boss, attackStyleImage, new Color(0, 0, 0, 0), 300);
            //Warn if prayer disabled.
            long timePassed = System.currentTimeMillis() - prayerWarning;
            long duration = config.getPrayerNotificationDuration();

            if(timePassed <= duration)
            {
                OverlayUtil.renderActorOverlay(graphics, client.getLocalPlayer(), "Pray!", new Color(255, 128, 1,255));
            }

            //If the player is not praying correctly, display the right player to pray.
            HeadIcon playerOverHead = client.getLocalPlayer().getOverheadIcon();

            Dimension size = client.getRealDimensions();

            if(attackStyle == GloveAttackStyle.RANGED && playerOverHead != HeadIcon.RANGED)
            {
                int halfScreenX = size.width / 2;
                int halfScreenY = size.height / 2;
                int halfX = PRAYER_WARNING_DIMENSION_X / 2;
                int halfY = PRAYER_WARNING_DIMENSION_Y / 2;

                Point screenPosition = new Point(halfScreenX - halfX, halfScreenY - halfY);
                bowComponent.setPreferredLocation(screenPosition);
                bowComponent.render(graphics);
                graphics.setColor(new Color(255, 255, 255, 255));
                graphics.drawRect(screenPosition.x, screenPosition.y, PRAYER_WARNING_DIMENSION_X, PRAYER_WARNING_DIMENSION_Y);
            }
            else if(attackStyle == GloveAttackStyle.MAGIC && playerOverHead != HeadIcon.MAGIC)
            {
                int halfScreenX = size.width / 2;
                int halfScreenY = size.height / 2;
                int halfX = PRAYER_WARNING_DIMENSION_X / 2;
                int halfY = PRAYER_WARNING_DIMENSION_Y / 2;

                Point screenPosition = new Point(halfScreenX - halfX, halfScreenY - halfY);
                staffComponent.setPreferredLocation(screenPosition);
                staffComponent.render(graphics);
                graphics.setColor(new Color(255, 255, 255, 255));
                graphics.drawRect(screenPosition.x, screenPosition.y, PRAYER_WARNING_DIMENSION_X, PRAYER_WARNING_DIMENSION_Y);
            }

            return d;

        }

        return null;
    }

    /**
     * Enable the overlay.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Update the overlay for the given fight instance.
     */
    public void update(GloveFight fight)
    {
        //Update location based on the boss
        boss = fight.getBoss();

        prayerWarning = fight.getLastPrayerWarning();

        //Update attack counter
        pieComponent.setProgress(((double) fight.getNpcAttackCount()) / 4d);

        if(attackStyleImage == null || attackStyle != fight.getStyle())
        {
            attackStyle = fight.getStyle();
            int itemID = 1;

            //Update item ID and pie color.
            if(attackStyle == GloveAttackStyle.MAGIC)
            {
                itemID = 11709;
                pieComponent.setBorder(new Color(86, 32, 123,255), 5);
                pieComponent.setFill(new Color(23, 122, 145, 255));
            }
            else
            {
                itemID = 4212;
                pieComponent.setBorder(new Color(73, 95, 61,255), 5);
                pieComponent.setFill(new Color(99, 137, 24, 255));
            }


            attackStyleImage = itemManager.getImage(itemID, 1, false);
            //attackStyleImage = ImageUtil.resizeImage(attackStyleImage, attackStyleImage.getWidth() * 1.3d, attackStyleImage.getHeight() * 1.3d);
        }
    }
}
