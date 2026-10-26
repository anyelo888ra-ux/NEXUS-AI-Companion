package ai.nexus.companion;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Lightweight client-side behavior prototype.
 * This is still a local ArmorStand, not a server-authoritative mob/player.
 */
final class NexusCompanionEntity {
    enum Behavior { FOLLOW, FREE_ROAM, STAY }

    private static ArmorStandEntity companion;
    private static int tickCounter;
    private static int roamTicks;
    private static double roamX;
    private static double roamZ;
    private static Behavior behavior = Behavior.FOLLOW;
    private static String currentTask = "Explorando contigo";
    private static final double FLOAT_AMPLITUDE = 0.18;
    private static final int FLOAT_CYCLE_TICKS = 240;
    private static final int FLOAT_DURATION_TICKS = 40;

    private NexusCompanionEntity() {}

    static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (companion == null || client.world == null || client.player == null) return;
            if (client.world.getEntityById(companion.getId()) != companion || companion.isRemoved()) {
                companion = null;
                return;
            }
            if (++tickCounter % 4 != 0) return;

            var player = client.player;
            double targetX = companion.getX();
            double targetZ = companion.getZ();
            if (behavior == Behavior.FOLLOW) {
                double radians = Math.toRadians(player.getYaw());
                targetX = player.getX() + Math.sin(radians) * 1.5;
                targetZ = player.getZ() - Math.cos(radians) * 1.5;
            } else if (behavior == Behavior.FREE_ROAM) {
                // Pick a new nearby point occasionally; keep it local and inexpensive.
                if (--roamTicks <= 0 || Math.hypot(companion.getX() - (player.getX() + roamX), companion.getZ() - (player.getZ() + roamZ)) < 0.7) {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    roamX = random.nextDouble(-4.0, 4.0);
                    roamZ = random.nextDouble(-4.0, 4.0);
                    roamTicks = random.nextInt(40, 100);
                }
                targetX = player.getX() + roamX;
                targetZ = player.getZ() + roamZ;
            }

            // Stay mode holds position. This is a visual prototype, so movement is interpolated lightly.
            if (behavior != Behavior.STAY) {
                double dx = targetX - companion.getX();
                double dz = targetZ - companion.getZ();
                double distance = Math.hypot(dx, dz);
                if (distance > 0.05) {
                    double step = Math.min(0.22, distance);
                    targetX = companion.getX() + dx / distance * step;
                    targetZ = companion.getZ() + dz / distance * step;
                }
            }

            int phase = tickCounter % FLOAT_CYCLE_TICKS;
            double floatOffset = phase < FLOAT_DURATION_TICKS
                ? Math.sin((phase / (double) FLOAT_DURATION_TICKS) * Math.PI) * FLOAT_AMPLITUDE
                : 0.0;
            companion.setPosition(targetX, player.getY() + floatOffset, targetZ);
        });
    }

    static boolean spawn() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return false;
        if (companion != null && !companion.isRemoved() && client.world.getEntityById(companion.getId()) == companion) {
            return true;
        }
        ArmorStandEntity stand = new ArmorStandEntity(EntityType.ARMOR_STAND, client.world);
        stand.setCustomName(Text.literal("NEXUS.AI"));
        stand.setCustomNameVisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setPosition(client.player.getX() + 1.0, client.player.getY(), client.player.getZ());
        stand.setYaw(client.player.getYaw());
        client.world.addEntity(stand);
        companion = stand;
        tickCounter = 0;
        return true;
    }

    static boolean setBehavior(String value) {
        try {
            behavior = Behavior.valueOf(value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static String behaviorName() {
        return switch (behavior) {
            case FOLLOW -> "te sigue";
            case FREE_ROAM -> "explora cerca y a veces se aleja";
            case STAY -> "se queda quieto";
        };
    }

    static void setTask(String task) {
        currentTask = task == null || task.isBlank() ? "Explorando contigo" : task.trim();
    }

    static String taskStatus() { return currentTask; }

    static boolean isSpawned() {
        return companion != null && !companion.isRemoved();
    }
}
