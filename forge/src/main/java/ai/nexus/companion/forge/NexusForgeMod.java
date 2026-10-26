package ai.nexus.companion.forge;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/**
 * Minimal Forge entry point for Minecraft Java 1.21.11.
 * This first milestone verifies that the mod loads and exposes a client command.
 */
@Mod(NexusForgeMod.MOD_ID)
public final class NexusForgeMod {
    public static final String MOD_ID = "nexus_companion";

    public NexusForgeMod() {
        MinecraftForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("nexus")
                .then(Commands.literal("status")
                    .executes(context -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        if (minecraft.player != null) {
                            minecraft.player.displayClientMessage(
                                Component.literal("[NEXUS] Forge 1.21.11 está cargado. Próximo paso: conectar la cuenta y crear el personaje."),
                                false
                            );
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
    }
}
