package ai.nexus.companion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

final class NexusSession {
    private static final String API = "https://nexus.ai-nex.duckdns.org";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private static String accessToken = "";
    private static String refreshToken = "";

    private NexusSession() {}

    static boolean isLoggedIn() { return !accessToken.isBlank(); }

    static void load() {
        try {
            Path path = sessionPath();
            if (Files.exists(path)) {
                JsonObject saved = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                accessToken = saved.has("access_token") ? saved.get("access_token").getAsString() : "";
                refreshToken = saved.has("refresh_token") ? saved.get("refresh_token").getAsString() : "";
            }
        } catch (Exception ignored) { accessToken = ""; refreshToken = ""; }
    }

    static void authenticate(String action, String email, String password, Consumer<String> callback) {
        if (!email.contains("@") || password.length() < 6) {
            callback.accept("Correo inválido o contraseña menor de 6 caracteres.");
            return;
        }
        callback.accept(action.equals("signup") ? "Creando cuenta..." : "Iniciando sesión...");
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("action", action);
                body.addProperty("email", email);
                body.addProperty("password", password);
                JsonObject result = post("/api/auth", body, null);
                if (!result.has("access_token") || result.get("access_token").isJsonNull()) {
                    callback.accept("La autenticación no devolvió sesión. Si registraste cuenta, revisa si Supabase exige confirmar el correo.");
                    return;
                }
                accessToken = result.get("access_token").getAsString();
                refreshToken = result.has("refresh_token") && !result.get("refresh_token").isJsonNull()
                    ? result.get("refresh_token").getAsString() : "";
                save();
                callback.accept("¡Sesión conectada! Los chats usarán tu cuenta y los créditos compartidos de NEXUS.AI.");
            } catch (Exception e) {
                callback.accept("No se pudo conectar: " + safeMessage(e));
            }
        }, "NEXUS-auth").start();
    }

    static void chat(String message, Consumer<String> callback) {
        if (!isLoggedIn()) {
            callback.accept("Primero inicia sesión: /nexus login <correo> <contraseña>.");
            return;
        }
        if (message.length() > 4000) {
            callback.accept("El mensaje supera los 4000 caracteres.");
            return;
        }
        callback.accept("Pensando...");
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("message", message);
                body.addProperty("characterPrompt", "You are the friendly NEXUS Minecraft companion: curious, kind, funny, and concise. Help the player safely. Do not claim to perform Minecraft actions; this chat version cannot control the world.");
                JsonObject result = post("/api/chat", body, accessToken);
                if (result.has("error")) {
                    callback.accept(result.get("error").getAsString());
                    return;
                }
                String reply = result.has("reply") ? result.get("reply").getAsString() : "El servidor no devolvió una respuesta.";
                if (reply.length() > 1500) reply = reply.substring(0, 1500) + "…";
                callback.accept(reply);
            } catch (Exception e) {
                callback.accept("Error hablando con NEXUS.AI: " + safeMessage(e));
            }
        }, "NEXUS-chat").start();
    }

    static void logout() {
        accessToken = "";
        refreshToken = "";
        try { Files.deleteIfExists(sessionPath()); } catch (IOException ignored) {}
    }

    private static JsonObject post(String path, JsonObject body, String bearer) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + path))
            .timeout(Duration.ofSeconds(45))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
        if (bearer != null && !bearer.isBlank()) builder.header("Authorization", "Bearer " + bearer);
        HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject json;
        try { json = JsonParser.parseString(response.body()).getAsJsonObject(); }
        catch (Exception e) { throw new IOException("Respuesta no válida del servidor (HTTP " + response.statusCode() + ")."); }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(json.has("error") ? json.get("error").getAsString() : "HTTP " + response.statusCode());
        }
        return json;
    }

    private static void save() throws IOException {
        Path path = sessionPath();
        Files.createDirectories(path.getParent());
        JsonObject json = new JsonObject();
        json.addProperty("access_token", accessToken);
        json.addProperty("refresh_token", refreshToken);
        Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
    }

    private static Path sessionPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("nexus-companion-session.json");
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) return e.getClass().getSimpleName();
        return message.length() > 180 ? message.substring(0, 180) : message;
    }
}
