package ai.nexus.companion;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

/**
 * Client-only visual prototype. The stand is local to this client and is not
 * sent to the server, so it is not yet a server-authoritative companion.
 */
final class NexusCompanionEntity {
    private static final int ENTITY_ID = -271821;
    private static ArmorStandEntity companion;
    private static int tickCounter;

    private NexusCompanionEntity() {}

    static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (companion == null || client.world == null || client.player == null) return;
            if (companion.getWorld() != client.world || companion.isRemoved()) {
                companion = null;
                return;
            }
            // Keep a small distance behind the player without teleporting every frame.
            if (++tickCounter % 4 == 0) {
                var player = client.player;
                double radians = Math.toRadians(player.getYaw());
                double x = player.getX() + Math.sin(radians) * 1.25;
                double z = player.getZ() - Math.cos(radians) * 1.25;
                companion.setPosition(x, player.getY(), z);
                companion.setYaw(player.getYaw());
            }
        });
    }

    static boolean spawn() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return false;
        if (companion != null && !companion.isRemoved() && companion.getWorld() == client.world) {
            companion.setPosition(client.player.getX() + 1.0, client.player.getY(), client.player.getZ());
            return true;
        }
        ArmorStandEntity stand = new ArmorStandEntity(EntityType.ARMOR_STAND, client.world);
        stand.setId(ENTITY_ID);
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

    static boolean isSpawned() {
        return companion != null && !companion.isRemoved();
    }
}
