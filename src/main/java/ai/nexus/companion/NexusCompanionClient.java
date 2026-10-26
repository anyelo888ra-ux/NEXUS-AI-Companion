package ai.nexus.companion;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.text.Text;

public final class NexusCompanionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NexusSession.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("nexus")
                .then(ClientCommands.literal("login")
                    .then(ClientCommands.argument("email", StringArgumentType.word())
                        .then(ClientCommands.argument("password", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String email = StringArgumentType.getString(ctx, "email");
                                String password = StringArgumentType.getString(ctx, "password");
                                NexusSession.authenticate("login", email, password, msg -> say(msg));
                                return 1;
                            }))))
                .then(ClientCommands.literal("register")
                    .then(ClientCommands.argument("email", StringArgumentType.word())
                        .then(ClientCommands.argument("password", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                String email = StringArgumentType.getString(ctx, "email");
                                String password = StringArgumentType.getString(ctx, "password");
                                NexusSession.authenticate("signup", email, password, msg -> say(msg));
                                return 1;
                            }))))
                .then(ClientCommands.literal("logout").executes(ctx -> {
                    NexusSession.logout();
                    say("Sesión local cerrada.");
                    return 1;
                }))
                .then(ClientCommands.literal("credits").executes(ctx -> {
                    say("La cuenta está " + (NexusSession.isLoggedIn() ? "conectada. Usa /nexus chat <mensaje> para hablar." : "desconectada. Usa /nexus login <correo> <contraseña>."));
                    return 1;
                }))
                .then(ClientCommands.literal("chat")
                    .then(ClientCommands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            NexusSession.chat(StringArgumentType.getString(ctx, "message"), msg -> say(msg));
                            return 1;
                        })))
            );
        });
    }

    private static void say(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.player != null) client.player.sendMessage(Text.literal("[NEXUS] " + message), false);
        });
    }
}
