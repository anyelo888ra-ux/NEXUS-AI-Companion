package ai.nexus.companion;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;

public final class NexusCompanionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NexusSession.load();
        NexusCompanionEntity.init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("nexus")
                .then(ClientCommandManager.literal("login")
                    .then(ClientCommandManager.argument("credentials", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String[] parts = splitCredentials(StringArgumentType.getString(ctx, "credentials"));
                            if (parts == null) {
                                say("Uso: /nexus login <correo> <contraseña>. Si tu correo tiene @, ahora se acepta sin comillas.");
                                return 0;
                            }
                            NexusSession.authenticate("login", parts[0], parts[1], NexusCompanionClient::say);
                            return 1;
                        })))
                .then(ClientCommandManager.literal("register")
                    .then(ClientCommandManager.argument("credentials", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String[] parts = splitCredentials(StringArgumentType.getString(ctx, "credentials"));
                            if (parts == null) {
                                say("Uso: /nexus register <correo> <contraseña>.");
                                return 0;
                            }
                            NexusSession.authenticate("signup", parts[0], parts[1], NexusCompanionClient::say);
                            return 1;
                        })))
                .then(ClientCommandManager.literal("logout").executes(ctx -> {
                    NexusSession.logout();
                    say("Sesión local cerrada.");
                    return 1;
                }))
                .then(ClientCommandManager.literal("status").executes(ctx -> {
                    var client = net.minecraft.client.MinecraftClient.getInstance();
                    String world = client.world == null ? "fuera de un mundo" : "en el mundo";
                    String companion = NexusCompanionEntity.isSpawned() ? "compañero: invocado, " + NexusCompanionEntity.behaviorName() : "compañero: no invocado";
                    say("NEXUS.AI | cuenta: " + (NexusSession.isLoggedIn() ? "sesión guardada" : "sin sesión") + " | " + world + " | " + companion + " | tarea: " + NexusCompanionEntity.taskStatus() + " | Fabric 1.21.11");
                    return 1;
                }))
                .then(ClientCommandManager.literal("spawn").executes(ctx -> {
                    boolean spawned = NexusCompanionEntity.spawn();
                    say(spawned ? "¡NEXUS.AI apareció! Prototipo visual local: el combate real todavía está en desarrollo." : "Entra primero a un mundo y vuelve a usar /nexus spawn.");
                    return spawned ? 1 : 0;
                }))
                .then(ClientCommandManager.literal("behavior")
                    .then(ClientCommandManager.literal("follow").executes(ctx -> {
                        NexusCompanionEntity.setBehavior("follow");
                        say("Modo: ahora te sigue.");
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("free").executes(ctx -> {
                        NexusCompanionEntity.setBehavior("free_roam");
                        say("Modo libre: explora cerca de ti y puede alejarse unos bloques.");
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("stay").executes(ctx -> {
                        NexusCompanionEntity.setBehavior("stay");
                        say("Modo quieto: NEXUS se queda en su posición.");
                        return 1;
                    })))
                .then(ClientCommandManager.literal("task")
                    .then(ClientCommandManager.argument("instruction", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String instruction = StringArgumentType.getString(ctx, "instruction");
                            NexusCompanionEntity.setTask(instruction);
                            say("Tarea guardada: " + instruction + ". Ojo: la ejecución real (minar, recoger y usar inventario) aún no está implementada.");
                            return 1;
                        })))
                .then(ClientCommandManager.literal("credits").executes(ctx -> {
                    say(NexusSession.isLoggedIn() ? "Tienes una sesión guardada. Usa /nexus chat <mensaje> para hablar." : "Sin sesión. Usa /nexus login <correo> <contraseña>.");
                    return 1;
                }))
                .then(ClientCommandManager.literal("chat")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            NexusSession.chat(StringArgumentType.getString(ctx, "message"), NexusCompanionClient::say);
                            return 1;
                        })))
            );
        });
    }

    private static String[] splitCredentials(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        int split = trimmed.indexOf(' ');
        if (split <= 0 || split == trimmed.length() - 1) return null;
        String identifier = trimmed.substring(0, split).trim();
        String password = trimmed.substring(split + 1).trim();
        if (identifier.isEmpty() || password.isEmpty()) return null;
        return new String[]{identifier, password};
    }

    private static void say(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.player != null) client.player.sendMessage(Text.literal("[NEXUS] " + message), false);
        });
    }
}
